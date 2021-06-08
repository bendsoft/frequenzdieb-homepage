package ch.frequenzdieb.subscription

import ch.frequenzdieb.common.DefaultHandlers.asServerResponse
import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.common.RequestParamReader.readQueryParam
import ch.frequenzdieb.common.Validators.Companion.checkSignature
import ch.frequenzdieb.common.Validators.Companion.validateEMail
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import ch.frequenzdieb.email.EmailService
import ch.frequenzdieb.security.SignatureFactory
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*
import java.net.URI

@Configuration
class SubscriptionHandler(
    @Value("\${host.frontend}") private val frontendHostAddress: String,
    private val subscriptionRepository: SubscriptionRepository,
    private val emailService: EmailService,
    private val signatureFactory: SignatureFactory
) {
    private val emailVerificationTitle = "E-Mail Verifizierung für Frequenzdieb.ch"

    suspend fun findFirstByEmail(req: ServerRequest): ServerResponse =
        req.readQueryParam("email")
            .validateEMail()
            .let {
                subscriptionRepository.findFirstByEmail(it)
                    .awaitSingleOrNull()
                    .asServerResponse()
            }

    suspend fun create(req: ServerRequest): ServerResponse =
        req.awaitBody(Subscription::class)
            .validateEntity()
            .let { newSubscription ->
                subscriptionRepository.insert(newSubscription)
                    .awaitSingle()
                    .let {
                        emailService.sendEmail(
                            emailAddress = it.email,
                            subject = emailVerificationTitle,
                            message = createSubscriptionConfirmationMessage(it.id)
                        )
                        created(URI.create("/subscription/${it.id}"))
                            .bodyValueAndAwait(it)
                    }
            }

    suspend fun resendConfirmation(req: ServerRequest): ServerResponse =
        subscriptionRepository.findById(req.pathVariable("id"))
            .awaitSingleOrNull()
            ?.let {
                emailService.sendEmail(
                    emailAddress = it.email,
                    subject = emailVerificationTitle,
                    message = createSubscriptionConfirmationMessage(it.id)
                )
            }
            .asServerResponse()

    suspend fun update(req: ServerRequest): ServerResponse =
        req.awaitBody(Subscription::class)
            .validateEntity()
            .let { subscriptionToUpdate ->
                subscriptionRepository.findById(subscriptionToUpdate.id)
                    .awaitSingleOrNull()
                    ?.let {
                        if (subscriptionToUpdate.email != it.email) {
                            it.isConfirmed = false
                            emailService.sendEmail(
                                emailAddress = it.email,
                                subject = emailVerificationTitle,
                                message = createSubscriptionConfirmationMessage(it.id)
                            )
                        }
                        subscriptionRepository.save(it)
                            .awaitSingle()
                    }
                    .asServerResponse()
            }

    private fun createSubscriptionConfirmationMessage(subscriptionId: String) = createHTML().
        html {
            body {
                p {
                    +"Bitte klicke den untenstehenden Link an, damit wir deine Registrierung bestätigen können."
                }
                a("${frontendHostAddress}/#/subscription/${subscriptionId}/confirm?signature=${signatureFactory.createSignature(subscriptionId)}") {
                    +"e-Mail bestätigen"
                }
            }
        }

    suspend fun deleteAllByEmail(req: ServerRequest): ServerResponse =
        req.readQueryParam("email")
            .validateEMail()
            .let {
                subscriptionRepository.deleteAllByEmail(it)
                    .awaitSingle()
                    .let { deleteCount ->
                        if (deleteCount > 0)
                            noContent().buildAndAwait()
                        else notFound().buildAndAwait()
                    }
            }

    suspend fun sendSubscriptionDeletionEMail(req: ServerRequest): ServerResponse =
        req.readQueryParam("email")
            .validateEMail()
            .let { email ->
                subscriptionRepository.findFirstByEmail(email)
                    .awaitSingleOrNull()
                    ?.let {
                        emailService.sendEmail(
                            emailAddress = it.email,
                            subject = "Bitte verifiziere die Löschung deiner e-Mail von der Frequenzdieb-Homepage",
                            message = createSubscriptionDeletionMessage(it.id)
                        )
                    }
                    .asServerResponse(emptyBody = true)
            }

    suspend fun confirmWithSignature(req: ServerRequest): ServerResponse =
        req.readQueryParam("signature")
            .let { signature ->
                subscriptionRepository.findById(req.pathVariable("id"))
                    .awaitSingleOrNull()
                    ?.let { subscription ->
                        subscription.apply {
                            checkSignature(signature)
                            validateWith(ErrorCode.SUBSCRIPTION_ALREADY_CONFIRMED) { it.isConfirmed }
                            isConfirmed = true
                            subscriptionRepository
                                .save(subscription)
                                .awaitSingle()
                        }
                    }
                    .asServerResponse()
            }

    suspend fun deleteWithSignature(req: ServerRequest): ServerResponse =
        req.readQueryParam("signature")
            .let { signature ->
                subscriptionRepository.findById(req.pathVariable("id"))
                    .awaitSingleOrNull()
                    ?.let { subscription ->
                            subscription.checkSignature(signature)
                            subscriptionRepository
                                .deleteById(subscription.id)
                                .awaitSingle()
                            noContent().buildAndAwait()
                    } ?: notFound().buildAndAwait()
            }

    private fun createSubscriptionDeletionMessage(subscriptionId: String) = createHTML().
        html {
            body {
                p {
                    br { +"Bitte klicke den untenstehenden Link an, damit wir deine e-Mail löschen können." }
                    +"Durch den Klick bestätigst du uns, dass die e-Mail tatsächlich dir gehört und du sie auch wirklich löschen möchtest."
                }
                p {
                    br { +"Du erhälst diese e-Mail, ohne dass du deine e-Mail von uns löschen wolltest?" }
                    +"Dann hat jemand anderes versucht dich von unserem Server zu löschen und du kannst diese e-Mail einfach ignorieren :)"
                }
                a("${frontendHostAddress}/#/subscription/${subscriptionId}/remove?signature=${signatureFactory.createSignature(subscriptionId)}") {
                    +"e-Mail endgültig löschen"
                }
            }
        }
}

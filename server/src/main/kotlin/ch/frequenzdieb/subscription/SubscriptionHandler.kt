package ch.frequenzdieb.subscription

import ch.frequenzdieb.common.RequestParamReader.readQueryParam
import ch.frequenzdieb.common.Validators.Companion.checkSignature
import ch.frequenzdieb.common.Validators.Companion.validateEMail
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import ch.frequenzdieb.email.EmailService
import ch.frequenzdieb.security.SignatureFactory
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import java.net.URI

@Configuration
class SubscriptionHandler(
    @Value("\${host.frontend}") private val frontendHostAddress: String,
    private val subscriptionRepository: SubscriptionRepository,
    private val emailService: EmailService,
    private val signatureFactory: SignatureFactory
) {
    fun findFirstByEmail(req: ServerRequest) =
        req.readQueryParam("email")
            .validateEMail()
            .flatMap {
                subscriptionRepository.findFirstByEmail(it)
                    .flatMap { subscription -> ok().bodyValue(subscription) }
                    .switchIfEmpty(notFound().build())
            }

    private val emailVerificationTitle = "E-Mail Verifizierung für Frequenzdieb.ch"

    fun create(req: ServerRequest) =
        req.bodyToMono(Subscription::class.java).validateEntity()
            .flatMap { newSubscription ->
                subscriptionRepository.insert(newSubscription)
                    .doOnSuccess {
                        emailService.sendEmail(
                            emailAddress = it.email,
                            subject = emailVerificationTitle,
                            message = createSubscriptionConfirmationMessage(it.id)
                        )
                    }
                    .flatMap {
                        created(URI.create("/subscription/${it.id}"))
                            .bodyValue(it)
                    }
            }

    fun resendConfirmation(req: ServerRequest) =
        subscriptionRepository.findById(req.pathVariable("id"))
            .doOnSuccess {
                emailService.sendEmail(
                    emailAddress = it.email,
                    subject = emailVerificationTitle,
                    message = createSubscriptionConfirmationMessage(it.id)
                )
            }
            .flatMap { ok().build() }
            .switchIfEmpty(notFound().build())

    fun update(req: ServerRequest) =
        req.bodyToMono(Subscription::class.java).validateEntity()
            .validateWith ("SUBSCRIPTION_INVALID_ID") { it.id.isNotEmpty() }
            .zipWhen { subscriptionRepository.findById(it.id) }
            .validateWith("SUBSCRIPTION_NOT_EXISTS") { it.t2.id.isNotEmpty() }
            .doOnNext {
                if (it.t1.email != it.t2.email) {
                    it.t2.isConfirmed = false
                    emailService.sendEmail(
                        emailAddress = it.t2.email,
                        subject = emailVerificationTitle,
                        message = createSubscriptionConfirmationMessage(it.t2.id)
                    )
                }
            }
           .flatMap {
                subscriptionRepository.save(it.t2)
                    .flatMap { savedSubscription -> ok().bodyValue(savedSubscription) }
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

    fun deleteAllByEmail(req: ServerRequest) =
        req.readQueryParam("email")
            .validateEMail()
            .flatMap {
                subscriptionRepository.deleteAllByEmail(it)
                    .filter { deleteCount -> deleteCount > 0 }
                    .flatMap { noContent().build() }
                    .switchIfEmpty(notFound().build())
            }

    fun sendSubscriptionDeletionEMail(req: ServerRequest) =
        req.readQueryParam("email")
            .validateEMail()
            .flatMap {
                subscriptionRepository.findFirstByEmail(it)
                    .flatMap { subscription ->
                        emailService.sendEmail(
                            emailAddress = subscription.email,
                            subject = "Bitte verifiziere die Löschung deiner e-Mail von der Frequenzdieb-Homepage",
                            message = createSubscriptionDeletionMessage(subscription.id)
                        )
                        ok().build()
                    }
            }

    fun confirmWithSignature(req: ServerRequest) =
        req.readQueryParam("signature")
            .flatMap { signature ->
                subscriptionRepository.findById(req.pathVariable("id"))
                    .checkSignature(signature)
                    .validateWith("ALREADY_CONFIRMED") { it.isConfirmed }
                    .flatMap {
                        it.isConfirmed = true
                        subscriptionRepository.save(it)
                            .flatMap { updatedSubscription -> ok().bodyValue(updatedSubscription) }
                    }
                    .switchIfEmpty(notFound().build())
            }

    fun deleteWithSignature(req: ServerRequest) =
        req.readQueryParam("signature")
            .flatMap { signature ->
                subscriptionRepository.findById(req.pathVariable("id"))
                    .checkSignature(signature)
                    .flatMap {
                        noContent().build(subscriptionRepository.deleteById(it.id))
                    }
                    .switchIfEmpty(notFound().build())
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

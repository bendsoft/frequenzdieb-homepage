package ch.frequenzdieb.api.services.subscription

import ch.frequenzdieb.api.services.common.EmailService
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class SubscriptionHandler {
    @Autowired
    lateinit var repository: SubscriptionRepository

    @Autowired
    lateinit var emailService: EmailService

    @Value("\${frequenzdieb.host}")
    lateinit var hostAddress: String

    fun findFirstByEmail(req: ServerRequest) =
        Mono.justOrEmpty(req.queryParam("email"))
            .flatMap {
                repository.findFirstByEmail(it)
                    .flatMap { subscription -> ok().bodyValue(subscription) }
                    .switchIfEmpty(notFound().build())
            }
            .switchIfEmpty(badRequest().bodyValue("Please provide an email"))

    fun confirm(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .filter { !it.isConfirmed }
            .flatMap {
                it.isConfirmed = true
                repository.save(it)
                    .flatMap { updatedSubscription -> ok().bodyValue(updatedSubscription) }
            }
            .switchIfEmpty(badRequest().bodyValue("Invalid confirmation request"))

    fun create(req: ServerRequest) =
        req.bodyToMono(Subscription::class.java)
            .flatMap { newSubscription ->
            repository.findFirstByEmail(newSubscription.email)
                    .flatMap { badRequest().bodyValue("E-Mail has already subscribed") }
                    .switchIfEmpty(
                        repository.save(newSubscription)
                            .doOnSuccess {
                                emailService.sendEmail(
                                    emailAddress = it.email,
                                    subject = "E-Mail Verifizierung für den Frequenzdieb-Newsletter",
                                    message = createSubscriptionConfirmationMessage(it.id!!)
                                )
                            }
                            .flatMap {
                                created(URI.create("/subscription/${it.id}"))
                                    .bodyValue(it)
                            }
                    )
            }

    private fun createSubscriptionConfirmationMessage(subscriptionId: String) = createHTML().
        html {
            body {
                p {
                    +"Bitte klicke den untenstehenden Link an, damit wir deine Registrierung bestätigen können."
                }
                a("${hostAddress}/api/subscription/${subscriptionId}/confirm") {
                    +"e-Mail bestätigen"
                }
            }
        }

    fun unsubscribe(req: ServerRequest) =
        changeSubscriptionToNewsletter(req, accepted = false)

    fun subscribe(req: ServerRequest) =
        changeSubscriptionToNewsletter(req, accepted = true)

    private fun changeSubscriptionToNewsletter(req: ServerRequest, accepted: Boolean) =
        repository.findById(req.pathVariable("id"))
            .flatMap {
                it.isNewsletterAccepted = accepted
                repository.save(it)
                    .flatMap { updatedSubscription ->
                        ok().bodyValue(updatedSubscription)
                    }
            }
            .switchIfEmpty(badRequest().build())

    fun deleteAllByEmail(req: ServerRequest) =
        Mono.justOrEmpty(req.queryParam("email"))
            .map { repository.deleteAllByEmail(it) }
            .flatMap {
                it
                    .filter { deleteCount -> deleteCount > 0 }
                    .flatMap { noContent().build() }
                    .switchIfEmpty(notFound().build())
            }
            .switchIfEmpty(badRequest().bodyValue("Please provide an email"))
}

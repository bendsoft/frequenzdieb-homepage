package ch.frequenzdieb.event.signup

import ch.frequenzdieb.common.RequestParamReader.readQueryParamAsync
import ch.frequenzdieb.common.Validators.Companion.validateAsyncWith
import ch.frequenzdieb.common.Validators.Companion.validateEMail
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import ch.frequenzdieb.event.EventRepository
import ch.frequenzdieb.subscription.SubscriptionRepository
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import java.net.URI

@Configuration
class SignUpHandler(
    private val repository: SignUpRepository,
    private val eventRepository: EventRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    fun findAll(req: ServerRequest) =
        ok().body(repository.findAll())
            .switchIfEmpty(notFound().build())

    fun create(req: ServerRequest) =
        req.bodyToMono(SignUp::class.java).validateEntity()
            .validateWith ("INVALID_EVENT_ID") { req.pathVariable("eventId") == it.event.id }
            .validateAsyncWith("INVALID_EVENT") {
                eventRepository.findById(req.pathVariable("eventId")).hasElement()
            }
            .validateAsyncWith("INVALID_EVENT") {
                subscriptionRepository.findById(req.pathVariable("subscriptionId")).hasElement()
            }
            .flatMap { repository.save(it)
                .flatMap { createdSignup ->
                    created(URI.create("/event/${it.event.id}/signup/${createdSignup.id}\""))
                        .bodyValue(it)
                }
            }

    fun deleteAllByEmail(req: ServerRequest) =
        req.readQueryParamAsync("email")
            .validateEMail()
            .flatMap { email ->
                subscriptionRepository.findFirstByEmail(email)
                    .flatMap { repository.deleteAllBySubscriptionId(it.id) }
                    .filter { it > 0 }
                    .flatMap { noContent().build() }
                    .switchIfEmpty(notFound().build())
            }
}

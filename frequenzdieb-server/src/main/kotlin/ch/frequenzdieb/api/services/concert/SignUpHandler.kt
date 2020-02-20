package ch.frequenzdieb.api.services.concert

import ch.frequenzdieb.api.services.subscription.SubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class SignUpHandler {
    @Autowired lateinit var repository: SignUpRepository
    @Autowired lateinit var concertRepository: ConcertRepository
    @Autowired lateinit var subscriptionRepository: SubscriptionRepository

    fun findAll(req: ServerRequest) =
        ok().body(repository.findAll())
            .switchIfEmpty(notFound().build())

    fun findById(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .flatMap { ok().bodyValue(it) }
            .switchIfEmpty(notFound().build())

    fun create(req: ServerRequest) =
        req.bodyToMono(SignUp::class.java)
            .filterWhen { concertRepository.findById(req.pathVariable("concertId")).hasElement() }
            .flatMap { repository.save(it)
                .flatMap { createdSignup ->
                    created(URI.create("/concert/${it.concertId}/signup/${createdSignup.id}\""))
                        .bodyValue(it)
                }
            }
            .switchIfEmpty(badRequest().bodyValue("SignUp entity must be valid and contain a concertId"))

    fun deleteAllByEmail(req: ServerRequest) =
        Mono.justOrEmpty(req.queryParam("email"))
            .flatMap {
                subscriptionRepository.findFirstByEmail(it)
                    .flatMap { subscription -> repository.deleteAllBySubscriptionId(subscription.id!!) }
                    .filter { deleteCount -> deleteCount > 0 }
                    .flatMap { noContent().build() }
                    .switchIfEmpty(notFound().build())
            }
            .switchIfEmpty(badRequest().bodyValue("Please provide an email"))
}

package ch.frequenzdieb.api.services.subscription

import org.springframework.beans.factory.annotation.Autowired
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

    fun findAllByEmail(req: ServerRequest) =
        Mono.justOrEmpty(req.queryParam("email"))
            .flatMap {
                repository.findFirstByEmail(it)
                    .flatMap { subscription -> ok().bodyValue(subscription) }
                    .switchIfEmpty(notFound().build())
            }
            .switchIfEmpty(badRequest().bodyValue("Please provide an email"))

    fun confirm(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .doOnNext {
                if (!it.isConfirmed) {
                    it.isConfirmed = true
                    repository.save(it)
                }
            }
            .flatMap { ok().bodyValue(it) }

    fun create(req: ServerRequest) =
        req.bodyToMono(Subscription::class.java)
            .flatMap {
                repository.findFirstByEmail(it.email)
                    .flatMap { badRequest().bodyValue("E-Mail has already subscribed") }
                    .switchIfEmpty(
                        repository.save(it)
                            .flatMap { subscription ->
                                created(URI.create("/subscription/${subscription.id}"))
                                    .bodyValue(subscription)
                            }
                    )
            }

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

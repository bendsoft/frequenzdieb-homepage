package ch.frequenzdieb.api.subscription

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class SubscriptionHandler {
    @Autowired
    lateinit var repository: SubscriptionRepository

    fun findAllByEmail(req: ServerRequest) =
        Mono.just(req.queryParam("email"))
            .filter { it.isPresent }
            .map { repository.findAllByEmail(it.get()) }
            .flatMap {
                it.next()
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
                repository.findAllByEmail(it.email).next()
                    .flatMap { badRequest().bodyValue("E-Mail has already subscribed") }
                    .switchIfEmpty(
                        repository.save(it)
                            .flatMap { subscription -> created(URI.create("/subscription/${subscription.id}")).build() }
                    )
            }

    fun deleteAllByEmail(req: ServerRequest) =
        Mono.just(req.queryParam("email"))
            .filter { it.isPresent }
            .map { repository.deleteAllByEmail(it.get()) }
            .flatMap {
                it
                    .flatMap { noContent().build() }
                    .switchIfEmpty(notFound().build())
            }
            .switchIfEmpty(badRequest().bodyValue("Please provide an email"))
}

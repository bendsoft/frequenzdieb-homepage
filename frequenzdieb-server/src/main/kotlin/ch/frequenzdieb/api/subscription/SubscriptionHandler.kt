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
            .flatMap {
                ok().body(repository.findAllByEmail(it.get()), Subscription::class.java)
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
            .flatMap { ok().body(it, Subscription::class.java) }

    fun create(req: ServerRequest) =
        req.bodyToMono(Subscription::class.java)
            .doOnNext { repository.save(it) }
            .flatMap { created(URI.create("/subscription/${it.id}")).build() }

    fun deleteAllByEmail(req: ServerRequest) =
        repository.deleteAllByEmail(req.queryParam("email").orElse(""))
            .flatMap { noContent().build() }
}

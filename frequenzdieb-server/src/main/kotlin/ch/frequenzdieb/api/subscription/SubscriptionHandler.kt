package ch.frequenzdieb.api.subscription

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import java.net.URI

@Configuration
class SubscriptionHandler {
    @Autowired
    lateinit var repository: SubscriptionRepository

    fun findAllByQuery(req: ServerRequest) =
        ok().body(repository.findAllByEmail(req.queryParam("email").orElse("")))
            .switchIfEmpty(notFound().build())

    fun findById(req: ServerRequest) =
        ok().body(repository.findById(req.pathVariable("id")))
            .switchIfEmpty(notFound().build())

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

    fun deleteByQuery(req: ServerRequest) =
        repository.deleteAllByEmail(req.queryParam("email").orElse(""))
            .flatMap { noContent().build() }
}

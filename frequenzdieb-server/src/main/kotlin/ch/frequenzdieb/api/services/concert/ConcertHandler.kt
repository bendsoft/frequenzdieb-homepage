package ch.frequenzdieb.api.services.concert

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import java.net.URI

@Configuration
class ConcertHandler {
    @Autowired
    lateinit var repository: ConcertRepository

    fun findAll(req: ServerRequest) =
        ok().body(repository.findAll())
            .switchIfEmpty(notFound().build())

    fun findById(req: ServerRequest) =
        ok().body(repository.findById(req.pathVariable("id")))
            .switchIfEmpty(notFound().build())

    fun create(req: ServerRequest) =
        req.bodyToMono(Concert::class.java)
            .doOnNext { repository.save(it) }
            .flatMap { created(URI.create("/concert/${it.id}")).build() }

    fun delete(req: ServerRequest) =
        repository.deleteById(req.pathVariable("id"))
            .flatMap { noContent().build() }
}
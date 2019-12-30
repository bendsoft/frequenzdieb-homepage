package ch.frequenzdieb.api.services.concert

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import java.net.URI

@Configuration
class SignUpHandler {
    @Autowired
    lateinit var repository: SignUpRepository

    fun findAll(req: ServerRequest) =
        ok().body(repository.findAll())
            .switchIfEmpty(notFound().build())

    fun findById(req: ServerRequest) =
        ok().body(repository.findById(req.pathVariable("id")))
            .switchIfEmpty(notFound().build())

    fun create(req: ServerRequest) =
        req.bodyToMono(SignUp::class.java)
            .doOnNext { repository.save(it) }
            .flatMap { created(URI.create("/concert/${it.concertId}/signup/${it.id}")).build() }

    fun delete(req: ServerRequest) =
        repository.deleteAllByEmail(req.queryParam("email").orElse(""))
            .flatMap { noContent().build() }
}

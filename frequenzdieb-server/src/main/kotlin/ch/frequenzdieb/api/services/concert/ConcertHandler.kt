package ch.frequenzdieb.api.services.concert

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
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
        repository.findById(req.pathVariable("id"))
            .flatMap { ok().bodyValue(it) }
            .switchIfEmpty(notFound().build())

    fun create(req: ServerRequest) =
        req.bodyToMono(Concert::class.java)
            .flatMap {
                repository.save(it)
                    .flatMap { concert ->
                        created(URI.create("/concert/${concert.id}"))
                            .bodyValue(concert)
                    }
            }
            .switchIfEmpty(badRequest().bodyValue("Concert entity must be provided"))

    fun deleteById(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .flatMap { concert ->
                repository.delete(concert)
                    .thenReturn(noContent().build())
                    .flatMap { it }
            }
            .switchIfEmpty(notFound().build())
}

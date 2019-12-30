package ch.frequenzdieb.api.services.ticketing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import java.net.URI

@Configuration
class TicketingHandler {
    @Autowired
    lateinit var repository: TicketingRepository

    fun findById(req: ServerRequest) =
        ok().body(repository.findById(req.pathVariable("id")))
            .switchIfEmpty(notFound().build())

    fun create(req: ServerRequest) =
        req.bodyToMono(Ticket::class.java)
            .doOnNext { repository.insert(it) }
            .flatMap { created(URI.create("/ticketing/${it.id}")).build() }

    fun invalidate(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .doOnNext {
                it.isValid = false
                repository.save(it)
            }
            .flatMap { ok().body(it, Ticket::class.java) }
}

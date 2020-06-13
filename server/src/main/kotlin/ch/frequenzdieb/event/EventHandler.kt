package ch.frequenzdieb.event

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body

@Configuration
class EventHandler {
    @Autowired
    lateinit var eventRepository: EventRepository

    fun findAll(req: ServerRequest) =
        eventRepository.findAll()
            .collectList()
            .flatMap { ok().bodyValue(it) }
            .switchIfEmpty(notFound().build())

    fun findById(req: ServerRequest) =
        eventRepository.findById(req.pathVariable("id"))
            .flatMap { ok().bodyValue(it) }
            .switchIfEmpty(notFound().build())

    fun deleteById(req: ServerRequest) =
        noContent().build(eventRepository.deleteById(req.pathVariable("id")))
}

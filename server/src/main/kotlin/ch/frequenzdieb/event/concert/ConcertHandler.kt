package ch.frequenzdieb.event.concert

import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.event.EventRepository
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import java.net.URI

@Configuration
class ConcertHandler(
    private val eventRepository: EventRepository
) {
    fun findAll(req: ServerRequest) =
        eventRepository.findAll()
            .filter { it is Concert }
            .collectMap { it }
            .flatMap { ok().bodyValue(it) }
            .switchIfEmpty(notFound().build())

    fun create(req: ServerRequest) =
        req.bodyToMono(Concert::class.java).validateEntity()
            .flatMap {
                eventRepository.save(it)
                    .flatMap { concert ->
                        created(URI.create("/event/concert/${concert.id}"))
                            .bodyValue(concert)
                    }
            }
}

package ch.frequenzdieb.event.concert

import ch.frequenzdieb.common.DefaultHandlers.asServerResponse
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.event.EventRepository
import ch.frequenzdieb.event.eventRoute
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.net.URI

@Configuration
class ConcertHandler(
    private val eventRepository: EventRepository
) {
    suspend fun findAll(req: ServerRequest): ServerResponse =
        eventRepository.findAll()
            .asFlow()
            .filter { it is Concert }
            .map { it as Concert }
            .toList()
            .asServerResponse()

    suspend fun create(req: ServerRequest): ServerResponse {
        val concert = req.awaitBody(Concert::class)

        return concert
            .validateEntity()
            .let {
                eventRepository.save(it)
                    .awaitSingle()
                    .let { concert ->
                        created(URI.create("$eventRoute/concert/${concert.id}"))
                            .bodyValueAndAwait(concert)
                    }
            }
    }
}

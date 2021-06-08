package ch.frequenzdieb.event

import ch.frequenzdieb.ticket.TicketType
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface EventRepository : ReactiveMongoRepository<Event, String> {
    fun existsById_AndTicketTypesContains(id: String, ticketType: TicketType): Mono<Boolean>
}

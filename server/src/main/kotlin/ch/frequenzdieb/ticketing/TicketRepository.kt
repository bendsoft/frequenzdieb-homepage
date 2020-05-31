package ch.frequenzdieb.ticketing

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TicketRepository : ReactiveMongoRepository<Ticket, String> {
	fun findAllBySubscription_IdAndEvent_Id(subscriptionId: String, eventId: String): Flux<Ticket>
	fun countTicketsByType_NameAndEvent_Id(typeId: String, eventId: String): Mono<Long>
}

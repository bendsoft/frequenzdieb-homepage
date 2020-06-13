package ch.frequenzdieb.ticketing

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TicketRepository : ReactiveMongoRepository<Ticket, String> {
	fun findAllBySubscriptionIdAndEventId(subscriptionId: String, eventId: String): Flux<Ticket>
	fun countTicketsByTypeIdAndEventId(typeId: String, eventId: String): Mono<Long>
}

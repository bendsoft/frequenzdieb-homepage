package ch.frequenzdieb.ticketing

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TicketRepository : ReactiveMongoRepository<Ticket, String> {
	fun findAllBySubscriptionId(subscriptionId: String): Flux<Ticket>
}

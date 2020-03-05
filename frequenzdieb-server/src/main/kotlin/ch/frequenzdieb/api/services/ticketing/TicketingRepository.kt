package ch.frequenzdieb.api.services.ticketing

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TicketingRepository : ReactiveMongoRepository<Ticket, String> {
    fun findAllBySubscriptionId(subscriptionId: String): Flux<Ticket>
    fun findOneByQrCode(qrCode: String): Mono<Ticket>
}

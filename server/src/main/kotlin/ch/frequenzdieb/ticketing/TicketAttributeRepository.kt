package ch.frequenzdieb.ticketing

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TicketAttributeRepository : ReactiveMongoRepository<TicketAttribute, String>

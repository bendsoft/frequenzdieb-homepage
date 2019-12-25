package ch.frequenzdieb.api.ticketing

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketingRepository : ReactiveMongoRepository<Ticket, String>

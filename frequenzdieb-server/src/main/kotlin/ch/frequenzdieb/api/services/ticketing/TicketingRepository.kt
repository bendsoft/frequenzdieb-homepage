package ch.frequenzdieb.api.services.ticketing

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketingRepository : ReactiveMongoRepository<Ticket, String>

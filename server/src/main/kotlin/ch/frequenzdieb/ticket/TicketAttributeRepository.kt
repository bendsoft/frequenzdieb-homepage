package ch.frequenzdieb.ticket

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketAttributeRepository : ReactiveMongoRepository<TicketAttribute, String>

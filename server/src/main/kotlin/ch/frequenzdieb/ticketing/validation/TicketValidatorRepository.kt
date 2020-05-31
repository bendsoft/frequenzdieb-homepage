package ch.frequenzdieb.ticketing.validation

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketValidatorRepository : ReactiveMongoRepository<TicketValidator, String>

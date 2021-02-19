package ch.frequenzdieb.ticket.archive

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TicketArchiveRepository : ReactiveMongoRepository<ArchivedTicket, String> {
    fun findByTicket_Id(ticketId: String): Mono<ArchivedTicket>
}

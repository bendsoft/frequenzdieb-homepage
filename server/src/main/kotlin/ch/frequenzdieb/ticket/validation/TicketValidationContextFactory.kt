package ch.frequenzdieb.ticket.validation

import ch.frequenzdieb.ticket.Ticket
import ch.frequenzdieb.ticket.TicketRepository
import org.springframework.stereotype.Component

@Component
class TicketValidationContextFactory(
    val ticketingRepository: TicketRepository
) {
    fun with(ticket: Ticket, contextConsumer: TicketValidationDsl.Context.() -> Unit) =
        TicketValidationDsl(ticket).Context(
            ticketingRepository,
            contextConsumer
        )
}

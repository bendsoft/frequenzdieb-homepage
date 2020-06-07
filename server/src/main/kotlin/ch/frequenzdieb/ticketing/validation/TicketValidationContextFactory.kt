package ch.frequenzdieb.ticketing.validation

import ch.frequenzdieb.ticketing.Ticket
import ch.frequenzdieb.ticketing.TicketRepository
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

package ch.frequenzdieb.ticket.validation

import ch.frequenzdieb.payment.PaymentService
import ch.frequenzdieb.ticket.Ticket
import ch.frequenzdieb.ticket.TicketRepository
import org.springframework.stereotype.Component

@Component
class ValidationContextFactory(
    val ticketRepository: TicketRepository,
    val paymentService: PaymentService<*>,
) {
    @ValidationDslMarker
    fun create(validateable: Ticket): TicketValidationContext {
        return TicketValidationContext(
            ticketRepository = ticketRepository,
            paymentService = paymentService,
            entity = validateable
        )
    }
}

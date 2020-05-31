package ch.frequenzdieb.ticketing.validation

import ch.frequenzdieb.common.ValidationError
import ch.frequenzdieb.ticketing.Ticket
import ch.frequenzdieb.ticketing.TicketType
import reactor.core.publisher.Mono

@DslMarker
annotation class TicketValidatorMarker

class TicketValidationDsl(
    private val context: TicketValidationContext,
    private val ticket: Ticket,
    private val validator: TicketValidationDsl.() -> Mono<ValidationError>
) {
    @TicketValidatorMarker
    infix fun Int.availableOfType(ticketTypeName: String) =
        context.ticketingRepository.countTicketsByType_NameAndEvent_Id(
            ticketTypeName,
            ticket.event.id!!
        )
            .filter { minus(it) <= 0.toLong() }
            .map {
                ValidationError(
                    code = "NO_MORE_TICKETS_AVAILABLE",
                    value = ticket.type
                )
            }

    internal fun build() = validator()
}

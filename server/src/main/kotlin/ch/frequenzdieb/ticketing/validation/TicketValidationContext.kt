package ch.frequenzdieb.ticketing.validation

import ch.frequenzdieb.common.ValidationError
import ch.frequenzdieb.ticketing.Ticket
import ch.frequenzdieb.ticketing.TicketRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun TicketValidationContext.rules(
    ticket: Ticket,
    validator: TicketValidationDsl.() -> Mono<ValidationError>
) = TicketValidationDsl(this, ticket, validator)
        .build()
        .concatWith { collectedErrors }

fun Flux<ValidationError>.process() =
    collectList()
        .filter { it.size > 0 }
        .map {
            ValidationError("TICKET_VALIDATION_FAILED", nested = it).throwAsServerResponse()
        }
        .subscribe {
            println(it)
        }

@Component
class TicketValidationContextFactory(
    val ticketingRepository: TicketRepository
) {
    fun create(param: TicketValidationContext.() -> Flux<ValidationError>) =
        TicketValidationContext(ticketingRepository).param().process()
}

class TicketValidationContext(
    val ticketingRepository: TicketRepository
) {
    val collectedErrors: Flux<ValidationError> = Flux.empty()
}

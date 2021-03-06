package ch.frequenzdieb.ticket.validation

import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.common.ValidationError
import ch.frequenzdieb.ticket.Ticket
import ch.frequenzdieb.ticket.TicketRepository
import reactor.core.publisher.Mono

@DslMarker
annotation class TicketValidationMarker

class TicketValidationDsl(
    private val ticket: Ticket
) {
    private val validators: MutableList<Mono<ValidationError>> = mutableListOf()
    private val collectedErrors: MutableList<ValidationError> = mutableListOf()

    inner class Context(
        private val ticketingRepository: TicketRepository,
        contextConsumer: TicketValidationDsl.Context.() -> Unit
    ) {
        init {
            contextConsumer()
        }

        @TicketValidationMarker
        fun rules(executeRules: TicketValidationDsl.() -> Unit) {
            this@TicketValidationDsl.apply {
                executeRules()
                waitForValidators()
                handleCollectedValidationErrors()
            }
        }

        @TicketValidationMarker
        infix fun Int.availableOfType(ticketTypeId: String) {
            val validator = ticketingRepository.countTicketsByTypeIdAndEventId(
                ticketTypeId,
                ticket.eventId
            )
                .filter { minus(it) <= 0.toLong() }
                .map { ValidationError(ErrorCode.NO_MORE_TICKETS_AVAILABLE) }

            validators.add(validator)
            validator.subscribe {
                collectedErrors.add(it)
            }
        }
    }

    private fun handleCollectedValidationErrors() {
        if (collectedErrors.isNotEmpty()) {
            ValidationError(
                code = ErrorCode.TICKET_VALIDATION_FAILED,
                nested = collectedErrors
            ).throwAsServerResponse()
        }
    }

    private fun waitForValidators() {
        Mono.`when`(validators)
            .doOnNext {
                println("All finished!")
            }
            .block()
    }
}

package ch.frequenzdieb.ticketing.validation

import ch.frequenzdieb.common.ValidationError
import ch.frequenzdieb.ticketing.Ticket
import ch.frequenzdieb.ticketing.TicketRepository
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
            val validator = ticketingRepository.countTicketsByType_IdAndEvent_Id(
                ticketTypeId,
                ticket.event.id!!
            )
                .filter { minus(it) <= 0.toLong() }
                .map {
                    ValidationError(
                        code = "NO_MORE_TICKETS_AVAILABLE",
                        value = ticket.type
                    )
                }

            validators.add(validator)
            validator.subscribe {
                collectedErrors.add(it)
            }
        }
    }

    private fun handleCollectedValidationErrors() {
        if (collectedErrors.isNotEmpty()) {
            ValidationError(
                code = "TICKET_VALIDATION_FAILED",
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

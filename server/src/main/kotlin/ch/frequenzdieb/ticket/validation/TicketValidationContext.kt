package ch.frequenzdieb.ticket.validation

import ch.frequenzdieb.payment.Payment
import ch.frequenzdieb.payment.PaymentService
import ch.frequenzdieb.ticket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrElse

@ValidationDslMarker
fun Ticket.validate(
    ticketRepository: TicketRepository,
    ticketTypeRepository: TicketTypeRepository,
    paymentService: PaymentService<*>,
    additionalRules: TicketValidationContext.() -> Unit = {}
) = TicketValidationContext(
    this,
    ticketRepository,
    ticketTypeRepository,
    paymentService
).apply {
    additionalRules()
}
typealias Validator = suspend (Pair<Ticket, TicketType>) -> Unit

class TicketValidationContext(
    private val ticket: Ticket,
    private val ticketRepository: TicketRepository,
    private val ticketTypeRepository: TicketTypeRepository,
    private val paymentService: PaymentService<*>
) : ValidationContext() {
    private val rulesOnOrder: MutableList<Validator> = mutableListOf()
    private val rulesOnInvalidation: MutableList<Validator> = mutableListOf()

    init {
        val validationRulesScript = ticket.type.validationRules.joinToString("\n")

        if (validationRulesScript.isNotBlank()) {
            executeScript(this, ticket.type.validationRules.joinToString("\n"))
        }
    }

    @ValidationDslMarker
    fun onOrder(validator: Validator) {
        rulesOnOrder.add(validator)
    }

    @ValidationDslMarker
    fun onInvalidation(validator: Validator) {
        rulesOnInvalidation.add(validator)
    }

    @ValidationDslMarker
    suspend fun executeOnOrder(): TicketValidationContext {
        rulesOnOrder
            .forEach { it(Pair(ticket, getTicketType())) }

        throwCollectedErrors()

        return this
    }

    @ValidationDslMarker
    suspend fun executeOnInvalidation(): TicketValidationContext {
        rulesOnInvalidation
            .forEach { it(Pair(ticket, getTicketType())) }

        throwCollectedErrors()

        return this
    }

    private suspend fun getTicketType() = ticketTypeRepository
        .findById(ticket.type.id)
        .awaitFirstOrElse { throw IllegalArgumentException("Could not find a ticket type with id ${ticket.type.id}. Please provide a ticket-type that exists.") }

    @ValidationDslMarker
    inline fun <reified T> TicketAttribute.get(property: String): T {
        val attributeValue = data[property]
        if (attributeValue is T)
            return attributeValue
        else throw IllegalArgumentException("The data-property named $property is of type ${attributeValue?.javaClass?.canonicalName}. Please use the correct type.")
    }

    @ValidationDslMarker
    fun TicketType.getAttribute(name: String): TicketAttribute =
        attributes.first { it.name == name }

    @ValidationDslMarker
    fun <R> TicketType.getAttribute(name: String, action: (TicketAttribute) -> R): R =
        attributes
            .first { it.name == name }
            .let { action(it) }

    @ValidationDslMarker
    suspend fun <R> Ticket.countSold(action: (Long) -> R): R =
        ticketRepository
            .countTicketsByType_NameAndEvent_Id(type.name, event.id)
            .awaitFirstOrDefault(0)
            .let { action(it) }

    @ValidationDslMarker
    fun Ticket.getSoldForSubscription(): Flow<Ticket> =
        ticketRepository
            .findAllBySubscription_IdAndEvent_Id(subscription.id, event.id)
            .asFlow()

    @ValidationDslMarker
    suspend fun <R> Ticket.getSoldForSubscription(
        attribute: TicketAttribute,
        action: suspend (Flow<Ticket>) -> R
    ): R =
        getSoldForSubscription()
            .filterByAttribute(attribute)
            .let { action(it) }

    @ValidationDslMarker
    fun Flow<Ticket>.filterByAttribute(attribute: TicketAttribute) =
        filter { soldTicket -> soldTicket.type.attributes.any { it == attribute } }

    @ValidationDslMarker
    suspend fun <R> Ticket.getLastPayment(action: (Payment) -> R): R =
        action(paymentService.loadValidPayment(id))
}

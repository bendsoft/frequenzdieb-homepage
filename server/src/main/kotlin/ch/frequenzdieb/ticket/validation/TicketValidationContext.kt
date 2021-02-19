package ch.frequenzdieb.ticket.validation

import ch.frequenzdieb.payment.Payment
import ch.frequenzdieb.payment.PaymentService
import ch.frequenzdieb.ticket.Ticket
import ch.frequenzdieb.ticket.TicketAttribute
import ch.frequenzdieb.ticket.TicketRepository
import ch.frequenzdieb.ticket.TicketType
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

class TicketValidationContext(
    private val ticketRepository: TicketRepository,
    private val paymentService: PaymentService<*>,
    val entity: Ticket,
) : AbstractValidationDsl() {
    override val validationRules: Map<Validateable, List<String>> =
        mapOf(
            entity to entity.validationRules,
            entity.type!! to entity.type!!.validationRules,
            *entity.type!!.attributes!!.map {
                it to it.validationRules
            }.toTypedArray()
        )

    @ValidationDslMarker
    lateinit var Ticket: Ticket

    @ValidationDslMarker
    var TicketType: TicketType? = null

    @ValidationDslMarker
    var TicketAttribute: TicketAttribute? = null

    override fun Validateable.onValidationHook() {
        TicketType = null
        TicketAttribute = null

        when (this) {
            is Ticket -> Ticket = this
            is TicketType -> TicketType = this
            is TicketAttribute -> TicketAttribute = this
        }
    }

    @ValidationDslMarker
    inline infix fun <reified T: Any> String.getDataPropertyAs(clazz: KClass<T>): T =
        if (TicketAttribute!!.data[this] is Comparable<*>)
            TicketAttribute!!.data[this] as T
        else throw IllegalArgumentException("The data-property named $this is of another type. Please use the correct type.")

    @ValidationDslMarker
    infix fun Ticket.getAttribute(name: String): TicketAttribute {
        val value = type?.attributes?.first { it.name == name }

        if (value != null)
            return value
        else throw IllegalArgumentException("Could not find an attrbute named $name.")
    }

    @ValidationDslMarker
    infix fun On.countSoldTicketsOf(ticket: Ticket): Int =
        runBlocking {
            ticketRepository.countTicketsByType_NameAndEventId(
                ticket.type!!.name,
                entity.eventId
            )
                .awaitFirst()
                .toInt()
        }

    @ValidationDslMarker
    infix fun On.getLastPaymentOf(ticket: Ticket): Payment =
        runBlocking {
            paymentService.loadValidPayment(ticket.id!!)
                .awaitFirstOrElse {
                    object : Payment {
                        override val amount = 0
                        override val currency = "None"
                    }
                }
        }
}

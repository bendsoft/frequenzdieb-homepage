package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper
import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.event.concert.ConcertHelper
import ch.frequenzdieb.payment.PaymentHelper
import ch.frequenzdieb.payment.PaymentService
import ch.frequenzdieb.security.SecurityHelper
import ch.frequenzdieb.security.configuration.SecurityConfig
import ch.frequenzdieb.ticket.validation.validate
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.inspectors.forOne
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.kotest.matchers.string.shouldContainIgnoringCase
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException

@WebFluxTest
@ComponentScan(basePackages = ["ch.frequenzdieb"])
@Import(value = [SecurityConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TicketTest {
    init {
        // This is only needed when running in IntelliJ and makes me sick!
        setIdeaIoUseFallback()
    }

    @Autowired lateinit var ticketService: TicketService
    @Autowired lateinit var ticketHelper: TicketHelper
    @Autowired lateinit var ticketTypeHelper: TicketTypeHelper
    @Autowired lateinit var ticketAttributeHelper: TicketAttributeHelper
    @Autowired lateinit var concertHelper: ConcertHelper
    @Autowired lateinit var securityHelper: SecurityHelper
    @Autowired lateinit var paymentHelper: PaymentHelper
    @Autowired lateinit var mongoReactiveTemplate: ReactiveMongoTemplate
    @Autowired lateinit var ticketRepository: TicketRepository
    @Autowired lateinit var ticketTypeRepository: TicketTypeRepository
    @Autowired lateinit var paymentService: PaymentService<*>

    private lateinit var restClient: SecurityHelper.AuthenticatedRestClient

    @BeforeAll
    fun setup() = runBlocking {
        BaseHelper.mongoReactiveTemplate = mongoReactiveTemplate
        restClient = securityHelper.initAccountsForRestClient()
    }

    @Test
    fun `when using script-engine, should throw a validation-error if no more tickets left`(): Unit = runBlocking {
        val concert = concertHelper.createConcert().insert()

        val fakeTicketAttribute = ticketAttributeHelper.createFakeAttribute(
            name = "Turnhalle Stehplätze",
            data = mapOf("Stehplätze" to 3)
        ).insert()

        val fakeTicketType = ticketTypeHelper.createTicketType(
            name = "testType",
            attributes = listOf(fakeTicketAttribute),
            validationRules = mutableListOf("""
                onOrder { (ticket, type) ->
                    ticket.countSold { sold ->
                        sold isMoreOrEqual type.getAttribute("Turnhalle Stehplätze").get<Int>("Stehplätze").toLong()
                    } raiseValidationError "NO_MORE_TICKETS_AVAILABLE"
                }
            """)
        ).insert()

        val fakeTickets = ticketHelper.createFakeTickets(3) {
            createFakeTicket(
                event = concert,
                type = fakeTicketType
            )
        }.insert()

        val fakeTicket = ticketHelper.createNewFakeTicketFrom(fakeTickets)

        val exception = shouldThrow<ResponseStatusException> {
            fakeTicket.validate(ticketRepository, ticketTypeRepository, paymentService).executeOnOrder()
        }

        exception.reason shouldContainIgnoringCase "VALIDATION_ERROR"
        exception.reason shouldContainIgnoringCase "NO_MORE_TICKETS_AVAILABLE"
    }

    @Test
    fun `when directly executed, should not throw a validation-error if still tickets left`(): Unit = runBlocking {
        val concert = concertHelper.createConcert().insert()

        val fakeTicketAttribute = ticketAttributeHelper.createFakeAttribute(
            name = "Turnhalle Stehplätze",
            data = mapOf("Stehplätze" to 100)
        ).insert()

        val fakeTicketType = ticketTypeHelper.createTicketType(
            name = "testType",
            attributes = listOf(fakeTicketAttribute)
        ).insert()

        val fakeTickets = ticketHelper.createFakeTickets(3) {
            createFakeTicket(
                event = concert,
                type = fakeTicketType
            )
        }.insert()

        val fakeTicket = ticketHelper.createNewFakeTicketFrom(fakeTickets)

        shouldNotThrow<ResponseStatusException> {
            fakeTicket.validate(ticketRepository, ticketTypeRepository, paymentService) {
                onOrder { (ticket, type) ->
                    ticket.countSold { sold ->
                        sold isMoreOrEqual type.getAttribute("Turnhalle Stehplätze").get<Int>("Stehplätze").toLong()
                    } raiseValidationError ErrorCode.NO_MORE_TICKETS_AVAILABLE.toString()
                }
            }.executeOnOrder()
        }
    }

    @Test
    fun `when directly executed, should throw a validation-error if no more tickets left`(): Unit = runBlocking {
        val concert = concertHelper.createConcert().insert()

        val fakeTicketAttribute = ticketAttributeHelper.createFakeAttribute(
            name = "Turnhalle Stehplätze",
            data = mapOf("Stehplätze" to 3)
        ).insert()

        val fakeTicketType = ticketTypeHelper.createTicketType(
            name = "testType",
            attributes = listOf(fakeTicketAttribute)
        ).insert()

        val fakeTickets = ticketHelper.createFakeTickets(3) {
            createFakeTicket(
                event = concert,
                type = fakeTicketType
            )
        }.insert()

        val fakeTicket = ticketHelper.createNewFakeTicketFrom(fakeTickets)

        val exception = shouldThrow<ResponseStatusException> {
            fakeTicket.validate(ticketRepository, ticketTypeRepository, paymentService) {
                onOrder { (ticket, type) ->
                    ticket.countSold { sold ->
                        sold isMoreOrEqual type.getAttribute("Turnhalle Stehplätze").get<Int>("Stehplätze").toLong()
                    } raiseValidationError ErrorCode.NO_MORE_TICKETS_AVAILABLE.toString()
                }
            }.executeOnOrder()
        }

        exception.reason shouldContainIgnoringCase "VALIDATION_ERROR"
        exception.reason shouldContainIgnoringCase ErrorCode.NO_MORE_TICKETS_AVAILABLE.toString()
    }

    @Test
    fun `when directly executed, should throw a validation-error if ticket has not been paid`(): Unit = runBlocking {
        val concert = concertHelper.createConcert().insert()

        val fakeTicketAttribute = ticketAttributeHelper.createFakeAttribute(
            name = "price",
            data = mutableMapOf("amount" to 3, "currency" to "CHF")
        ).insert()

        val fakeTicketType = ticketTypeHelper.createTicketType(
            name = "testType",
            attributes = listOf(fakeTicketAttribute)
        ).insert()

        val fakeTickets = ticketHelper.createFakeTickets(3) {
            createFakeTicket(
                event = concert,
                type = fakeTicketType
            )
        }.insert()

        val fakeTicket = ticketHelper.createNewFakeTicketFrom(fakeTickets)

        val exception = shouldThrow<ResponseStatusException> {
            fakeTicket.validate(ticketRepository, ticketTypeRepository, paymentService) {
                onInvalidation { (ticket, type) ->
                    ticket.getLastPayment { payment ->
                        type.getAttribute("price") { price ->
                            payment.amount isMoreOrEqual price.get("amount") || payment.currency isNotEqual price.get("currency")
                        }
                    } raiseValidationError ErrorCode.TICKET_NOT_PAID.toString()
                }
            }.executeOnInvalidation()
        }

        exception.reason shouldContainIgnoringCase "VALIDATION_ERROR"
        exception.reason shouldContainIgnoringCase ErrorCode.TICKET_NOT_PAID.toString()
    }

    @Test
    fun `should throw a validation-error if ticket has not been paid`(): Unit = runBlocking {
        val concert = concertHelper.createConcert().insert()

        val ticketType = ticketTypeHelper.createTicketType(
            name = "testType",
            validationRules = mutableListOf("""
                onInvalidation { (ticket, type) ->
                    ticket.getLastPayment { payment ->
                        with(type.getAttribute("price")) {
                            payment.amount isMoreOrEqual get("amount") || payment.currency isNotEqual get("currency")
                        }
                    } raiseValidationError "TICKET_NOT_PAID"
                }
            """),
            attributes = listOf(
                ticketAttributeHelper.createFakeAttribute(
                    name = "price",
                    data = mutableMapOf("amount" to 3, "currency" to "CHF")
                ).insert()
            )
        ).insert()

        val fakeTickets = ticketHelper.createFakeTickets(3) {
            createFakeTicket(
                event = concert,
                type = ticketType
            )
        }.insert()

        val fakeTicket = ticketHelper.createNewFakeTicketFrom(fakeTickets)

        val exception = shouldThrow<ResponseStatusException> {
            fakeTicket.validate(ticketRepository, ticketTypeRepository, paymentService).executeOnInvalidation()
        }

        exception.reason shouldContainIgnoringCase "VALIDATION_ERROR"
        exception.reason shouldContainIgnoringCase "TICKET_NOT_PAID"
    }

    @Test
    fun `should throw a validation-error if ordered too much tickets per subscriber`(): Unit = runBlocking {
        val concert = concertHelper.createConcert().insert()

        val standingRoomTicketAttribute = ticketAttributeHelper.createFakeAttribute(
            name ="Standing Room",
            data = mutableMapOf(
                "maxStandingTicketsPerPerson" to 10,
                "standingRoom" to 20
            )
        ).insert()

        val seatTicketAttribute = ticketAttributeHelper.createFakeAttribute(
            name ="Seat",
            data = mutableMapOf(
                "maxSeatTicketsPerPerson" to 3,
                "seats" to 100
            )
        ).insert()

        val fakeTicketType = ticketTypeHelper.createTicketType(
            name = "Frequenzdieb 2052",
            attributes = listOf(standingRoomTicketAttribute, seatTicketAttribute)
        ).insert()

        val fakeTicket = ticketHelper.createFakeTicket(
            event = concert,
            type = fakeTicketType
        )

        ticketHelper.createFakeTickets(3) {
            createNewFakeTicketFrom(fakeTicket)
        }.insert()

        val newFakeTicket = ticketHelper.createNewFakeTicketFrom(fakeTicket)

        val exception = shouldThrow<ResponseStatusException> {
            newFakeTicket.validate(ticketRepository, ticketTypeRepository, paymentService) {
                onOrder { (ticket, type) ->
                    ticket.getSoldForSubscription().apply {
                        type.getAttribute("Seat").let { seatAttribute ->
                            filterByAttribute(seatAttribute).count() isMoreOrEqual seatAttribute.get("maxSeatTicketsPerPerson")
                        } raiseValidationError ErrorCode.TOO_MUCH_TICKETS_ORDERED.toString()
                    }

                    type.getAttribute("Standing Room").let { standingRoomAttribute ->
                        ticket.getSoldForSubscription(standingRoomAttribute) { soldStandingRoomTickets ->
                            soldStandingRoomTickets.count() isMoreOrEqual standingRoomAttribute.get("maxStandingTicketsPerPerson")
                        } raiseValidationError ErrorCode.TOO_MUCH_TICKETS_ORDERED.toString()
                    }

                }
            }.executeOnOrder()
        }

        exception.reason shouldContainIgnoringCase "VALIDATION_ERROR"
        exception.reason shouldContainIgnoringCase ErrorCode.TOO_MUCH_TICKETS_ORDERED.toString()
    }

    @Test
    fun `when creating a ticket should create a valid ticket`(): Unit = runBlocking {
        val fakeTicket = ticketHelper.createNewFakeTicketFrom(ticketHelper.createFakeTicket())

        restClient.getAuthenticatedAsUser()
            .post().uri(ticketRoute)
            .bodyValue(fakeTicket)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody(TicketQrCode::class.java)
            .returnResult()
            .run {
                responseBody shouldNotBe null
                responseBody?.qrCode shouldNotBe null
                responseBody?.qrCode shouldNotBe ""
                responseBody?.qrCode!!
            }
            .let { createdTicketQRCode ->
                restClient.getAuthenticatedAsAdmin()
                    .get().uri {
                        it
                            .path(ticketRoute)
                            .queryParam("subscriptionId", fakeTicket.subscription.id)
                            .queryParam("eventId", fakeTicket.event.id)
                            .build()
                    }
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(Ticket::class.java)
                    .returnResult()
                    .apply {
                        responseBody shouldNotBe null
                        responseBody!!.forOne {
                            ticketService.createQRCode(it) shouldBeEqualIgnoringCase createdTicketQRCode
                        }
                    }
            }
    }

    @Test
    fun `when invalidating a ticket should set ticket to invalid`(): Unit = runBlocking {
        val fakeTicket = ticketHelper.createFakeTicket().insert()

        paymentHelper.insertTransaction(
            reference = fakeTicket.id,
            amount = 10,
            currency = "CHF"
        )

        restClient.getAuthenticatedAsAdmin()
            .put().uri("$ticketRoute/invalidate")
            .bodyValue(
                TicketQrCode(
                    qrCode = ticketService.encoder(fakeTicket.id.toByteArray())
                )
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isValid")
            .value<Boolean> { it shouldBe false }
    }
}

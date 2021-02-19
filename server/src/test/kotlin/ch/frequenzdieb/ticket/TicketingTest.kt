package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper
import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.event.concert.ConcertHelper
import ch.frequenzdieb.payment.PaymentHelper
import ch.frequenzdieb.security.SecurityHelper
import ch.frequenzdieb.security.configuration.SecurityConfig
import ch.frequenzdieb.ticket.validation.Error
import ch.frequenzdieb.ticket.validation.On
import ch.frequenzdieb.ticket.validation.ValidationContextFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.inspectors.forOne
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.kotest.matchers.string.shouldContainIgnoringCase
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
internal class TicketingTest {
    init {
        // This is only needed when running in IntelliJ and makes me sick!
        setIdeaIoUseFallback()
    }

    @Autowired lateinit var ticketService: TicketService
    @Autowired lateinit var ticketingHelper: TicketingHelper
    @Autowired lateinit var validationContextFactory: ValidationContextFactory
    @Autowired lateinit var concertHelper: ConcertHelper
    @Autowired lateinit var securityHelper: SecurityHelper
    @Autowired lateinit var paymentHelper: PaymentHelper
    @Autowired lateinit var mongoReactiveTemplate: ReactiveMongoTemplate

    private lateinit var restClient: SecurityHelper.AuthenticatedRestClient

    @BeforeAll
    fun setup() = runBlocking {
        BaseHelper.mongoReactiveTemplate = mongoReactiveTemplate
        restClient = securityHelper.initAccountsForRestClient()
    }

    @Test
    fun `should throw a validation-error if no more tickets left`(): Unit = runBlocking {
        val concert = concertHelper.createConcert().insert()

        val typeName = "testType"
        val fakeTickets = ticketingHelper.createFakeTickets(3) {
            createFakeTicket(
                event = concert,
                type = ticketTypeHelper.createTicketType(
                    name = typeName,
                    validationRules = mutableListOf("On.Order soldTicketsOfType \"$typeName\" lessThan 3 respondWithError Error.NO_MORE_TICKETS_AVAILABLE")
                ).insert()
            )
        }.insert()

        val fakeTicket = fakeTickets
            .first().copy()
            .apply { id = null }

        val exception = shouldThrow<ResponseStatusException> {
            validationContextFactory
                .create(fakeTicket)
                .validate()
        }

        exception.reason shouldContainIgnoringCase "VALIDATION_ERROR"
        exception.reason shouldContainIgnoringCase "NO_MORE_TICKETS_AVAILABLE"
    }

    @Test
    fun `should throw a validation-error if tickets has not been paid`(): Unit = runBlocking {
        val concert = concertHelper.createConcert().insert()

        val typeName = "testType"
        val fakeTickets = ticketingHelper.createFakeTickets(3) {
            createFakeTicket(
                event = concert,
                type = ticketTypeHelper.createTicketType(
                    name = typeName,
                    validationRules = mutableListOf(""),
                    attributes = listOf(
                        ticketAttributeHelper.createFakeAttribute(
                            validationRules = mutableListOf("""
                                On.Invalidation getLastPaymentOf Ticket thenCheck {
                                    "amount" getDataPropertyAs Int::class isLessOrEqual amount
                                        && "currency" getDataPropertyAs String::class isEqual currency
                                } raiseError Error.TICKET_NOT_PAID
                            """),
                            data = mutableMapOf("amount" to 15, "currency" to "CHF")
                        )
                    )
                ).insert()
            )
        }.insert()

        val fakeTicket = fakeTickets
            .first().copy()
            .apply { id = null }

        with (validationContextFactory.create(fakeTicket)) {
             On.Invalidation getLastPaymentOf Ticket thenCheck {
                 "amount" getDataPropertyAs Int::class isLessOrEqual amount
                     && "currency" getDataPropertyAs String::class isEqual currency
             } raiseError Error.TICKET_NOT_PAID
        }

        with (validationContextFactory.create(fakeTicket)) {
            On.Order countSoldTicketsOf Ticket thenCheck {
                isMoreOrEqual ("stock" getDataPropertyAs Int::class)
            } raiseError Error.NO_MORE_TICKETS_AVAILABLE
        }

        // TODO: Check if too much Tickets for one subscriber
        /*
        with (ticketValidationContextFactory.create(fakeTicket)) {
            On.Order soldTicketsOf Ticket
        }
        */

        val exception = shouldThrow<ResponseStatusException> {
            validationContextFactory
                .create(fakeTicket)
                .validate()
        }

        exception.reason shouldContainIgnoringCase "VALIDATION_ERROR"
        exception.reason shouldContainIgnoringCase "NO_MORE_TICKETS_AVAILABLE"
    }

    @Test
    fun `when creating a ticket should create a valid ticket`(): Unit = runBlocking {
        val fakeTicket = ticketingHelper.createFakeTicket()

        restClient.getAuthenticatedAsUser()
            .post().uri(ticketRoute)
            .bodyValue(fakeTicket)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody(TicketCreateResponse::class.java)
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
                            .queryParam("subscriptionId", fakeTicket.subscriptionId)
                            .queryParam("eventId", fakeTicket.eventId)
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
        val fakeTicket = ticketingHelper.createFakeTicket().insert()

        paymentHelper.insertTransaction(
            reference = fakeTicket.id!!,
            amount = 10,
            currency = "CHF"
        )

        restClient.getAuthenticatedAsAdmin()
            .put().uri("$ticketRoute/invalidate")
            .bodyValue(TicketInvalidationRequest(
                qrCodeValue = ticketService.encoder(fakeTicket.id!!.toByteArray()),
                eventId = fakeTicket.eventId
            ))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isValid")
            .value<Boolean> { it shouldBe false }
    }
}

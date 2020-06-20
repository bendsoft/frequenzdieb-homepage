package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.common.BaseIntegrationTest
import ch.frequenzdieb.event.concert.ConcertHelper
import ch.frequenzdieb.payment.PaymentHelper
import ch.frequenzdieb.security.SecurityHelper
import ch.frequenzdieb.ticket.validation.TicketValidationContextFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.inspectors.forOne
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException
import javax.script.ScriptEngineManager

internal class TicketingTest(
    private val ticketService: TicketService,
    private val ticketingHelper: TicketingHelper,
    private val ticketTypeHelper: TicketTypeHelper,
    private val validationContextFactory: TicketValidationContextFactory,
    private val concertHelper: ConcertHelper,
    private val securityHelper: SecurityHelper,
    private val paymentHelper: PaymentHelper
) : BaseIntegrationTest({
    // This is only needed when running in IntelliJ and makes me sick!
    setIdeaIoUseFallback()

    val restClient = securityHelper.initAccountsForRestClient()

    describe("using a validation rule on a ticket") {
        it("should throw a validation-error if no more tickets left") {
            val typeName = "testType"

            val event = concertHelper.createConcert().insert()
            val type = ticketTypeHelper.createTicketType(
                name = typeName,
                validationRules = listOf("3 availableOfType \"$typeName\"")
            ).insert()

            val fakeTickets = ticketingHelper.createFakeTicket(3) {
                createFakeTicket(
                    event = event,
                    type = type
                )
            }.insert()

            val fakeTicket = fakeTickets
                .first().copy()
                .apply { id = null }

            val scriptEngine = ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223JvmLocalScriptEngine

            val script =
                """
                3 availableOfType "${type.id}"
                """.trimIndent()

            shouldThrow<ResponseStatusException> {
                validationContextFactory.with(fakeTicket) {
                    rules {
                        scriptEngine.apply {
                            put("scope", this@with)
                            eval("""
                            with (bindings["scope"] as ch.frequenzdieb.ticketing.validation.TicketValidationDsl.Context) {
                                $script
                            }
                            """.trimIndent()
                            )
                        }
                    }
                }
            }
        }
    }

    describe("when creating a ticket") {
        it("should create a valid ticket") {
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
                        .get().uri { it
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

        describe("when invalidating a ticket") {
            it("should set ticket to invalid") {
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
    }
})

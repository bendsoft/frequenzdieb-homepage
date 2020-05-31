package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.common.BaseIntegrationTest
import ch.frequenzdieb.common.ValidationError
import ch.frequenzdieb.ticketing.validation.TicketValidationContextFactory
import ch.frequenzdieb.ticketing.validation.rules
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

internal class TicketingTest : BaseIntegrationTest() {
    @Autowired lateinit var ticketService: TicketService
    @Autowired lateinit var ticketingHelper: TicketingHelper
    @Autowired lateinit var ticketTypeHelper: TicketTypeHelper
    @Autowired lateinit var contextFactory: TicketValidationContextFactory

    init {
        describe("using a validation rule on a ticket") {
            it("should throw a validation-error if no more tickets left") {
                val typeName = "testType"

                val fakeTickets = ticketingHelper.createFakeStandingTicket(3) {
                    createFakeStandingTicket(
                        type = ticketTypeHelper.createTicketType(
                            name = typeName,
                            validationRules = listOf("3 availableOfType \"$typeName\"")
                        )
                    )
                }.insert()

                val fakeTicket = fakeTickets
                    .first().copy()
                    .apply { id = null }

                val scriptEngine: ScriptEngine = ScriptEngineManager().getEngineByExtension("kts")

                val script = "3 availableOfType \"$typeName\""

                contextFactory.create {
                    rules(fakeTicket) {
                        scriptEngine.apply {
                            setBindings()
                            eval(script) as Mono<ValidationError>
                        }
                    }
                }
             }
        }

        /*
        describe("when creating a ticket") {
            it("should create a valid ticket") {
                val fakeTicket = ticketingHelper.createFakeStandingTicket()

                getRestClientUnauthenticated()
                    .post().uri(ticketingRoute)
                    .bodyValue(hashMapOf(
                        "concert" to fakeTicket.event,
                        "subscription" to fakeTicket.subscription
                    ))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody(TicketCreateResponse::class.java)
                    .returnResult()
                    .apply { responseBody?.qrCode?.let {
                        ticketService.decoder(it) shouldBeEqualIgnoringCase fakeTicket.id
                    } }
            }

            describe("when invalidating a ticket") {
                it("should set ticket to invalid") {
                    val fakeTicket = ticketingHelper.createFakeStandingTicket()

                    // TODO: insert a payment for the ticket

                    getRestClientAuthenticatedWithAdmin()
                        .put().uri("$ticketingRoute/invalidate")
                        .bodyValue(TicketInvalidationRequest(
                            qrCodeValue = ticketService.createQRCode(fakeTicket),
                            eventId = fakeTicket.event.id
                        ))
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk
                        .expectBody(Ticket::class.java)
                        .returnResult()
                        .apply { responseBody?.isValid shouldBe false }
                }
            }
        }
        */
    }
}

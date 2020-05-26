package ch.frequenzdieb.ticketing

import ch.frequenzdieb.api.BaseIntegrationTest
import ch.frequenzdieb.event.concert.ConcertHelper
import ch.frequenzdieb.subscription.SubscriptionHelper
import org.springframework.http.MediaType

internal class TicketingTest(
    private val concertHelper: ConcertHelper,
    private val subscriptionHelper: SubscriptionHelper,
    private val ticketService: TicketService
) : BaseIntegrationTest() {

    private fun prepareData() =
        hashMapOf(
            "concertId" to concertHelper.insertConcert(),
            "subscriptionId" to subscriptionHelper.insertSubscriptionForHans("Muster")
        )

    init {
        describe("when creating a ticket") {
            it("should create a valid ticket") {
                getRestClientUnauthenticated()
                    .post().uri("/api/ticket")
                    .bodyValue(prepareData())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody(TicketCreateResponse::class.java)
            }

            describe("when invalidating a ticket") {
                it("should set ticket to invalid") {

                }
            }
        }
    }
}

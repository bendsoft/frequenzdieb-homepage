package ch.frequenzdieb.event.concert

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.common.BaseHelper.Dsl.resetCollection
import ch.frequenzdieb.common.BaseIntegrationTest
import ch.frequenzdieb.event.eventRoute
import ch.frequenzdieb.event.location.Location
import ch.frequenzdieb.security.SecurityHelper
import ch.frequenzdieb.ticket.TicketTypeHelper
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ConcertIntegrationTest(
    @Autowired private val concertHelper: ConcertHelper,
    @Autowired private val securityHelper: SecurityHelper,
    @Autowired private val ticketTypeHelper: TicketTypeHelper
) : BaseIntegrationTest() {
    private lateinit var restClient: SecurityHelper.AuthenticatedRestClient
    private lateinit var concert: Concert

    private val randomConcertName = createRandomString(10)
    private val randomLocation = Location(createRandomString(10))
    private val randomLiveActs = listOf(createRandomString(10))
    private val randomTerms = createRandomString(10)

    @BeforeAll
    fun setup() = runBlocking {
        restClient = securityHelper.createAuthenticatedRestClient()

        //existing concert
        resetCollection(Concert::class)
        concert = concertHelper.createConcert(
            concertName = randomConcertName
        )
    }

    @Test
    fun `should have randomConcertName as name`(): Unit = runBlocking {
        concert.insert()

        restClient.unauthenticated()
            .get().uri("$eventRoute/concert/${concert.id}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(Concert::class.java)
            .returnResult()
            .apply { responseBody?.name shouldBe randomConcertName }
    }

    @Test
    fun `when getting all should have randomConcertName as name`(): Unit = runBlocking {
        concert.insert()

        restClient.unauthenticated()
            .get().uri("$eventRoute/concert")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Concert::class.java)
            .returnResult()
            .apply { responseBody?.first()?.name shouldBe randomConcertName }
    }

    @Test
    fun `should return 404 if not found`() {
        restClient.authenticatedAsAdmin()
            .get().uri("$eventRoute/concert/doesnotexist")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    // deleting a concert
    @Test
    fun `should not allow unauthenticated deletion`(): Unit = runBlocking {
        concert.insert()

        restClient.unauthenticated()
            .delete().uri("$eventRoute/concert/${concert.id}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should delete a concert`(): Unit = runBlocking {
        concert.insert()

        restClient.authenticatedAsAdmin()
            .delete().uri("$eventRoute/concert/${concert.id}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent
            .apply {
                restClient.authenticatedAsAdmin()
                    .get().uri("$eventRoute/concert/${concert.id}")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound
            }
    }

    //creating a new concert
    @Test
    fun `should not allow unauthenticated creation`(): Unit = runBlocking {
        val fakeConcert = Concert(
            name = randomConcertName,
            location = randomLocation,
            date = LocalDateTime.now().plusYears(1),
            liveActs = randomLiveActs,
            terms = randomTerms,
            ticketTypes = listOf(ticketTypeHelper.createTicketType())
        )

        restClient.unauthenticated()
            .post().uri("$eventRoute/concert")
            .bodyValue(fakeConcert)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should contain the concert`(): Unit = runBlocking {
        val fakeConcert = Concert(
            name = randomConcertName,
            location = randomLocation,
            date = LocalDateTime.now().plusYears(1),
            liveActs = randomLiveActs,
            terms = randomTerms,
            ticketTypes = listOf(ticketTypeHelper.createTicketType())
        )

        restClient.authenticatedAsAdmin()
            .post().uri("$eventRoute/concert")
            .bodyValue(fakeConcert)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody(Concert::class.java)
            .returnResult()
            .apply {
                restClient.authenticatedAsAdmin()
                    .get().uri("$eventRoute/concert/${responseBody?.id}")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Concert::class.java)
                    .returnResult()
                    .apply { responseBody?.name shouldBe randomConcertName }
            }
    }
}

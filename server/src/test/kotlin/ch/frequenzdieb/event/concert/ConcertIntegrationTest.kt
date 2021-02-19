package ch.frequenzdieb.event.concert

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import ch.frequenzdieb.common.BaseHelper.Dsl.resetCollection
import ch.frequenzdieb.common.BaseIntegrationTest
import ch.frequenzdieb.event.location.LocationHelper
import ch.frequenzdieb.security.SecurityHelper
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ConcertIntegrationTest : BaseIntegrationTest() {
    @Autowired lateinit var concertHelper: ConcertHelper
    @Autowired lateinit var locationHelper: LocationHelper
    @Autowired lateinit var securityHelper: SecurityHelper

    private val randomConcertName = createRandomString(10)
    private val randomLocation = locationHelper.createLocation()
    private val randomLiveActs = listOf(createRandomString(10))
    private val randomTerms = createRandomString(10)

    private lateinit var restClient: SecurityHelper.AuthenticatedRestClient
    private lateinit var concert: Concert

    @BeforeAll
    fun setup() = runBlockingTest {
        restClient = securityHelper.initAccountsForRestClient()

        //existing concert
        resetCollection(Concert::class.java)
        concert = concertHelper.createConcert(
            concertName = randomConcertName
        )
    }

    @Test
    fun `should have $randomConcertName as name`() {
        restClient.getAuthenticatedAsAdmin()
            .get().uri("/api/concert/${concert.id}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(Concert::class.java)
            .returnResult()
            .apply { responseBody?.name shouldBe randomConcertName }
    }

    @Test
    fun `should return 404 if not found`() {
        restClient.getAuthenticatedAsAdmin()
            .get().uri("/api/concert/doesnotexist")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    // deleting a concert
    @Test
    fun `should not allow unauthenticated deletion`() {
        restClient.getAuthenticatedAsAdmin()
            .delete().uri("/api/concert/${concert.id}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should delete a concert`() {
        val authenticatedRestClient = restClient.getAuthenticatedAsAdmin()

        authenticatedRestClient
            .delete().uri("/api/concert/${concert.id}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent
            .apply {
                authenticatedRestClient
                    .get().uri("/api/concert/${concert.id}")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound
            }
    }


    //creating a new concert
    @Test
    fun `should not allow unauthenticated creation`() {
        securityHelper.getRestClientUnauthenticated()
            .post().uri("/api/concert")
            .bodyValue(Concert(
                name = randomConcertName,
                location = randomLocation,
                date = LocalDateTime.of(2099, 5, 2, 0, 0),
                liveActs = randomLiveActs,
                terms = randomTerms
            ))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should contain the concert`() {
        val authenticatedRestClient = restClient.getAuthenticatedAsAdmin()

        authenticatedRestClient
            .post().uri("/api/concert")
            .bodyValue(Concert(
                name = randomConcertName,
                location = randomLocation,
                date = LocalDateTime.of(2099, 5, 2, 0, 0),
                liveActs = randomLiveActs,
                terms = randomTerms
            ))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody(Concert::class.java)
            .returnResult()
            .apply {
                authenticatedRestClient
                    .get().uri("/api/concert/${responseBody?.id}")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Concert::class.java)
                    .returnResult()
                    .apply { responseBody?.name shouldBe randomConcertName }
            }
    }
}

package ch.frequenzdieb.event.concert

import ch.frequenzdieb.common.BaseIntegrationTest
import ch.frequenzdieb.event.location.LocationHelper
import ch.frequenzdieb.security.SecurityHelper
import io.kotest.matchers.shouldBe
import org.springframework.http.MediaType
import java.time.LocalDateTime

internal class ConcertIntegrationTest(
    private val concertHelper: ConcertHelper,
    private val locationHelper: LocationHelper,
    private val securityHelper: SecurityHelper
) : BaseIntegrationTest({
    val randomConcertName = concertHelper.createRandomString(10)
    val randomLocation = locationHelper.createLocation()
    val randomLiveActs = listOf(concertHelper.createRandomString(10))
    val randomTerms = concertHelper.createRandomString(10)

    val restClient = securityHelper.initAccountsForRestClient()

    describe("existing concert") {
        concertHelper.resetCollection()
        val insertedId = concertHelper.createConcert(
            concertName = randomConcertName
        )

        it("should have $randomConcertName as name") {
            restClient.getAuthenticatedAsAdmin()
                .get().uri("/api/concert/$insertedId")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(Concert::class.java)
                .returnResult()
                .apply { responseBody?.name shouldBe randomConcertName }
        }

        it("should return 404 if not found") {
            restClient.getAuthenticatedAsAdmin()
                .get().uri("/api/concert/doesnotexist")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound
        }

        describe("deleting a concert") {
            it("should not allow unauthenticated deletion") {
                restClient.getAuthenticatedAsAdmin()
                    .delete().uri("/api/concert/$insertedId")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isUnauthorized
            }

            it("should delete a concert") {
                val authenticatedRestClient = restClient.getAuthenticatedAsAdmin()

                authenticatedRestClient
                    .delete().uri("/api/concert/$insertedId")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNoContent
                    .apply {
                        authenticatedRestClient
                            .get().uri("/api/concert/$insertedId")
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange()
                            .expectStatus().isNotFound
                    }
            }
        }
    }

    describe("creating a new concert") {
        it("should not allow unauthenticated creation") {
            securityHelper.getRestClientUnauthenticated()
                .post().uri("/api/concert")
                .bodyValue(Concert(
                    name = randomConcertName,
                    locationId = randomLocation.id!!,
                    date = LocalDateTime.of(2099, 5, 2, 0, 0),
                    liveActs = randomLiveActs,
                    terms = randomTerms
                ))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized
        }

        it("should contain the concert") {
            val authenticatedRestClient = restClient.getAuthenticatedAsAdmin()

            authenticatedRestClient
                .post().uri("/api/concert")
                .bodyValue(Concert(
                    name = randomConcertName,
                    locationId = randomLocation.id!!,
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
})

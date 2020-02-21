package ch.frequenzdieb.api.services.concert

import ch.frequenzdieb.api.BaseIntegrationTest
import io.kotlintest.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate
import java.time.LocalDateTime

internal class ConcertIntegrationTest : BaseIntegrationTest() {
    @Autowired
    lateinit var concertHelper: ConcertHelper

    init {
        describe("existing concert") {
            concertHelper.resetCollection()
            val insertedId = concertHelper.insertConcert()

            it("should have SmmerComeBackXX as name") {
                getRestClientAuthenticatedWithAdmin()
                    .get().uri("/api/concert/$insertedId")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Concert::class.java)
                    .returnResult()
                    .apply { responseBody?.name shouldBe "SmmerComeBackXX" }
            }

            it("should return 404 if not found") {
                getRestClientAuthenticatedWithAdmin()
                    .get().uri("/api/concert/doesnotexist")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound
            }

            describe("deleting a concert") {
                it("should not allow unauthenticated deletion") {
                    getRestClientUnauthenticated()
                        .delete().uri("/api/concert/$insertedId")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isUnauthorized
                }

                it("should delete a concert") {
                    getRestClientAuthenticatedWithAdmin()
                        .delete().uri("/api/concert/$insertedId")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isNoContent
                        .apply {
                            getRestClientAuthenticatedWithAdmin()
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
                getRestClientUnauthenticated()
                    .post().uri("/api/concert")
                    .bodyValue(Concert(
                        name = "SmmerComeBack2099",
                        location = "Tatooine",
                        date = LocalDate.of(2099, 5, 2)
                    ))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isUnauthorized
            }

            it("should contain the concert") {
                getRestClientAuthenticatedWithAdmin()
                    .post().uri("/api/concert")
                    .bodyValue(Concert(
                        name = "SmmerComeBack2099",
                        location = "Tatooine",
                        date = LocalDate.of(2099, 5, 2)
                    ))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody(Concert::class.java)
                    .returnResult()
                    .apply {
                        getRestClientAuthenticatedWithAdmin()
                            .get().uri("/api/concert/${responseBody?.id}")
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange()
                            .expectStatus().isOk
                            .expectBody(Concert::class.java)
                            .returnResult()
                            .apply { responseBody?.name shouldBe "SmmerComeBack2099" }
                    }
            }
        }
    }
}
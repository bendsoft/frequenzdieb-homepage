package ch.frequenzdieb.api.services.subscription

import ch.frequenzdieb.api.BaseIntegrationTest
import io.kotlintest.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class SubscriptionIntegrationTest : BaseIntegrationTest() {
    @Autowired
    lateinit var subscriptionHelper: SubscriptionHelper

    init {
        describe("get subscription by email of hans muster") {
            val subscriberName = subscriptionHelper.createRandomString(5)

            subscriptionHelper.resetCollection()
            subscriptionHelper.insertSubscriptionForHans(subscriberName)

            it("should not allow unauthenticated requests") {
                getRestClientUnauthenticated()
                    .get().uri("/api/subscription?email=hans.muster@example.com")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isUnauthorized
            }

            it("should have Hans as surname") {
                getRestClientAuthenticatedWithAdmin()
                    .get().uri("/api/subscription?email=hans.muster@example.com")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Subscription::class.java)
                    .returnResult()
                    .apply { responseBody?.surname shouldBe "Hans" }
            }

            it("should return 404 if not found") {
                getRestClientAuthenticatedWithAdmin()
                    .get().uri("/api/subscription?email=han.solo@example.com")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound
            }

            it("should be a bad-request if no email given") {
                getRestClientAuthenticatedWithAdmin()
                    .get().uri("/api/subscription")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody(String::class.java)
                    .returnResult()
                    .apply { responseBody shouldBe "Please provide an email" }
            }

            it("should delete a request by email given") {
                getRestClientUnauthenticated()
                    .delete().uri("/api/subscription?email=hans.muster@example.com")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNoContent
                    .apply {
                        getRestClientAuthenticatedWithAdmin()
                            .get().uri("/api/subscription?email=hans.muster@example.com")
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange()
                            .expectStatus().isNotFound
                    }
            }

            describe("creating a new subscription") {
                it("should contain the subscription") {
                    createMaxImal()
                        .expectStatus().isCreated
                        .apply {
                            getRestClientAuthenticatedWithAdmin()
                                .get().uri("/api/subscription?email=max.imal@example.com")
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk
                                .expectBody(Subscription::class.java)
                                .returnResult()
                                .apply { responseBody?.surname shouldBe "Imal" }
                        }
                }

                it("should not allow duplicates") {
                    createMaxImal()
                        .expectStatus().isBadRequest
                        .expectBody(String::class.java)
                        .returnResult()
                        .apply { responseBody shouldBe "E-Mail has already subscribed" }
                }
            }
        }
    }

    private fun createMaxImal(): WebTestClient.ResponseSpec {
        return getRestClientUnauthenticated()
            .post().uri("/api/subscription")
            .bodyValue(Subscription(
                surname = "Imal",
                name = "Max",
                email = "max.imal@example.com"
            ))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
    }
}

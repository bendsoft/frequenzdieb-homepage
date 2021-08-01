package ch.frequenzdieb.subscription

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import ch.frequenzdieb.common.BaseHelper.Dsl.resetCollection
import ch.frequenzdieb.common.BaseIntegrationTest
import ch.frequenzdieb.security.SecurityHelper
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class SubscriptionIntegrationTest : BaseIntegrationTest() {
    @Autowired lateinit var subscriptionHelper: SubscriptionHelper
    @Autowired lateinit var securityHelper: SecurityHelper

    private lateinit var restClient: SecurityHelper.AuthenticatedRestClient

    fun createMaxImal(): WebTestClient.ResponseSpec {
        return securityHelper.unauthenticated()
            .post().uri("/api/subscription")
            .bodyValue(Subscription(
                surname = "Imal",
                name = "Max",
                email = "max.imal@example.com"
            ))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
    }

    @ExperimentalCoroutinesApi
    @BeforeAll
    fun setup() = runBlockingTest {
        // get subscription by email of hans muster
        val subscriberName = createRandomString(5)
        resetCollection(Subscription::class)
        subscriptionHelper.createSubscriptionForHans(subscriberName)
        restClient = securityHelper.createAuthenticatedRestClient()
    }

    @Test
    fun `should not allow unauthenticated requests`() {
        securityHelper.unauthenticated()
            .get().uri("/api/subscription?email=hans.muster@example.com")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should have Hans as surname`() {
        restClient.authenticatedAsAdmin()
            .get().uri("/api/subscription?email=hans.muster@example.com")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(Subscription::class.java)
            .returnResult()
            .apply { responseBody?.surname shouldBe "Hans" }
    }

    @Test
    fun `should return 404 if not found`() {
        restClient.authenticatedAsAdmin()
            .get().uri("/api/subscription?email=han.solo@example.com")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should be a bad-request if no email given`() {
        restClient.authenticatedAsAdmin()
            .get().uri("/api/subscription")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(String::class.java)
            .returnResult()
            .apply { responseBody shouldBe "Please provide an email" }
    }

    @Test
    fun `should delete a request by email given`() {
        securityHelper.unauthenticated()
            .delete().uri("/api/subscription?email=hans.muster@example.com")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent
            .apply {
                restClient.authenticatedAsAdmin()
                    .get().uri("/api/subscription?email=hans.muster@example.com")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound
            }
    }

    @Test
    fun `creating a new subscription should contain the subscription`() {
        createMaxImal()
            .expectStatus().isCreated
            .apply {
                restClient.authenticatedAsAdmin()
                    .get().uri("/api/subscription?email=max.imal@example.com")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Subscription::class.java)
                    .returnResult()
                    .apply { responseBody?.surname shouldBe "Imal" }
            }
    }

    @Test
    fun `creating a new subscription should not allow duplicates`() {
        createMaxImal()
            .expectStatus().isBadRequest
            .expectBody(String::class.java)
            .returnResult()
            .apply { responseBody shouldBe "E-Mail has already subscribed" }
    }
}

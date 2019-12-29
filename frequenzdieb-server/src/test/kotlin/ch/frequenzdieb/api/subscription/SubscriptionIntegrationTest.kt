package ch.frequenzdieb.api.subscription

import ch.frequenzdieb.api.BaseIntegrationTest
import io.kotlintest.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.MediaType

@ComponentScan(basePackages = ["ch.frequenzdieb.api.subscription"])
internal class SubscriptionIntegrationTest : BaseIntegrationTest() {
    @Autowired
    lateinit var subscriptionHelper: SubscriptionHelper

    init {
        describe("get subscription by email of hans muster") {
            subscriptionHelper.resetCollection()
            subscriptionHelper.insertSubscriptionForHansMuster()

            it("should have Muster as name") {
                restClient.get().uri("/api/subscription/query?email=hans.muster@example.com")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Subscription::class.java)
                    .returnResult()
                    .apply { responseBody?.name shouldBe "Muster" }
            }

            it("should return 404 if not found") {
                restClient.get().uri("/api/subscription/query?email=han.solo@example.com")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound
            }

            it("should be a bad-request if no email given") {
                restClient.get().uri("/api/subscription/query")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody(String::class.java)
                    .returnResult()
                    .apply { responseBody shouldBe "Please provide an email" }
            }
        }
    }
}

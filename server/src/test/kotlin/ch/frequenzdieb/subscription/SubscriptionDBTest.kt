package ch.frequenzdieb.subscription

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.forOne
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.ComponentScan

@DataMongoTest
@ComponentScan(basePackages = ["ch.frequenzdieb.common.subscription"])
internal class SubscriptionDBTest : DescribeSpec() {
    @Autowired
    lateinit var subscriptionHelper: SubscriptionHelper

    init {
        describe("get subscription by email of hans muster") {
            subscriptionHelper.resetCollection()
            subscriptionHelper.createSubscriptionForHans("Muster")

            it("should have inserted the email") {
                subscriptionHelper.getAllSubscriptions().forOne { it.email shouldBe "hans.muster@example.com" }
            }
        }
    }
}

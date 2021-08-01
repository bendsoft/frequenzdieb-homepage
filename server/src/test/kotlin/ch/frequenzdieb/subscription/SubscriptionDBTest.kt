package ch.frequenzdieb.subscription

import ch.frequenzdieb.common.BaseHelper.Dsl.resetCollection
import io.kotest.inspectors.forOne
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.ComponentScan

@DataMongoTest
@ComponentScan(basePackages = ["ch.frequenzdieb.common.subscription"])
internal class SubscriptionDBTest {
    @Autowired
    lateinit var subscriptionHelper: SubscriptionHelper

    @BeforeAll
    fun setup() = runBlocking {
        //get subscription by email of hans muster
        resetCollection(Subscription::class)
        subscriptionHelper.createSubscriptionForHans("Muster")
    }

    @Test
    fun `should have inserted the email`() = runBlocking {
        subscriptionHelper.getAllSubscriptions().forOne {
            subscription -> subscription.email shouldBe "hans.muster@example.com" }
    }
}

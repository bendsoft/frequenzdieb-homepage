package ch.frequenzdieb.subscription

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Component

@Component
@DataMongoTest
internal class SubscriptionHelper {
    @Autowired lateinit var reactiveMongoTemplate: ReactiveMongoTemplate

    fun createSubscriptionForHans(subscriberName: String): Subscription =
        Subscription(
            name = subscriberName,
            surname = "Hans",
            email = "${createRandomString(5)}.${subscriberName}@example.com",
            isNewsletterAccepted = true,
            isConfirmed = true
        )

    suspend fun getAllSubscriptions(): List<Subscription> =
        reactiveMongoTemplate.findAll(Subscription::class.java)
            .collectList()
            .awaitFirst()
}

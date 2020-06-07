package ch.frequenzdieb.subscription

import ch.frequenzdieb.common.BaseHelper
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class SubscriptionHelper(
    mongoTemplate: MongoTemplate
) : BaseHelper(mongoTemplate, Subscription::class.java) {
    fun createSubscriptionForHans(subscriberName: String): Subscription =
        Subscription(
            name = subscriberName,
            surname = "Hans",
            email = "${createRandomString(5)}.${subscriberName}@example.com",
            isNewsletterAccepted = true,
            isConfirmed = true
        )

    fun getAllSubscriptions(): MutableList<Subscription> =
        mongoTemplate.findAll(Subscription::class.java)
}

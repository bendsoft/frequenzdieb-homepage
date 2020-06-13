package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseHelper
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.event.concert.Concert
import ch.frequenzdieb.event.concert.ConcertHelper
import ch.frequenzdieb.subscription.Subscription
import ch.frequenzdieb.subscription.SubscriptionHelper
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class TicketAttributeHelper(
    mongoTemplate: MongoTemplate
) : BaseHelper(mongoTemplate, Ticket::class.java) {
    fun createFakeAttribute() = TicketAttribute(
        key = createRandomString(5),
        value = createRandomString(5),
        tag = createRandomString(5),
        text = createRandomString(5)
    )

    fun createFakeAttribute(
        amount: Int,
        attributeProducer: TicketAttributeHelper.() -> TicketAttribute = { createFakeAttribute() }
    ): List<TicketAttribute> =
        (1..amount).map {
            attributeProducer()
        }
}

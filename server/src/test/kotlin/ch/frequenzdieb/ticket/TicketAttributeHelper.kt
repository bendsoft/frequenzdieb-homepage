package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper
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

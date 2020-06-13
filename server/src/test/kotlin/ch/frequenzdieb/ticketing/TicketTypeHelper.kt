package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseHelper
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class TicketTypeHelper(
    mongoTemplate: MongoTemplate,
    private val ticketAttributeHelper: TicketAttributeHelper
) : BaseHelper(mongoTemplate, TicketType::class.java) {
    fun createTicketType(
        name: String = createRandomString(5),
        attributes: List<TicketAttribute> = ticketAttributeHelper.createFakeAttribute(2).insert(),
        validationRules: List<String> = emptyList()
    ) = TicketType(
        name = name,
        attributeIds = attributes.map { it.id!! },
        validationRules = validationRules
    )

    fun createTicketType(
        amount: Int,
        typeProducer: TicketTypeHelper.() -> TicketType = { createTicketType() }
    ) =
        (1..amount).map {
            typeProducer()
        }
}

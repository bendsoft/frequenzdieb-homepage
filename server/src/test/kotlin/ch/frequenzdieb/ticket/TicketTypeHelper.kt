package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class TicketTypeHelper {
    @Autowired lateinit var ticketAttributeHelper: TicketAttributeHelper

    suspend fun createTicketType(
        name: String = createRandomString(5),
        attributes: List<TicketAttribute>? = null,
        validationRules: MutableList<String> = mutableListOf()
    ) = TicketType(
        name = name,
        attributeIds = (ticketAttributeHelper.createFakeAttribute(2).insert()).map { it.id!! },
        validationRules = validationRules
    )

    suspend fun createTicketType(
        amount: Int,
        typeProducer: suspend TicketTypeHelper.() -> TicketType = { createTicketType() }
    ) =
        (1..amount).map {
            typeProducer()
        }
}

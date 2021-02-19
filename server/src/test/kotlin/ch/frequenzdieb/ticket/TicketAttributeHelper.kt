package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class TicketAttributeHelper {
    fun createFakeAttribute(
        validationRules: MutableList<String> = mutableListOf(),
        data: MutableMap<String, Any> = mutableMapOf()
    ) = TicketAttribute(
        key = createRandomString(5),
        name = createRandomString(5),
        tag = createRandomString(5),
        text = createRandomString(5),
        validationRules = validationRules,
        data = data
    )

    fun createFakeAttribute(
        amount: Int,
        attributeProducer: TicketAttributeHelper.() -> TicketAttribute = { createFakeAttribute() }
    ): List<TicketAttribute> =
        (1..amount).map {
            attributeProducer()
        }
}

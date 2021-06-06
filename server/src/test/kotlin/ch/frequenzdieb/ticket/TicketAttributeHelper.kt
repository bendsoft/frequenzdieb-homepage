package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class TicketAttributeHelper {
    fun createFakeAttribute(
        name: String = createRandomString(5),
        data: Map<String, Any> = mapOf(),
        validationRules: List<String> = listOf(),
        tag: TicketAttributeTag = TicketAttributeTag(
            name = createRandomString(5),
            text = createRandomString(5),
            key = createRandomString(5)
        )
    ) = TicketAttribute(
        name = name,
        tag = tag,
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

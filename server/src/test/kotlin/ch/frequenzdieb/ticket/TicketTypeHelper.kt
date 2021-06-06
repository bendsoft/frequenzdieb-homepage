package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.event.concert.ConcertHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class TicketTypeHelper {
    @Autowired lateinit var ticketAttributeHelper: TicketAttributeHelper

    suspend fun createTicketType(
        name: String = createRandomString(5),
        event: Event? = null,
        attributes: List<TicketAttribute> = listOf(),
        validationRules: MutableList<String> = mutableListOf()
    ): TicketType {
        val fakeAttributes = attributes.ifEmpty { ticketAttributeHelper.createFakeAttribute(2).insert() }

        return TicketType(
            name = name,
            attributes = fakeAttributes,
            validationRules = validationRules
        )
    }

    suspend fun createTicketType(
        amount: Int,
        typeProducer: suspend TicketTypeHelper.() -> TicketType = { createTicketType() }
    ) =
        (1..amount).map {
            typeProducer()
        }
}

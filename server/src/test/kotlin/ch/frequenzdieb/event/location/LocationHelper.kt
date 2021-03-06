package ch.frequenzdieb.event.location

import ch.frequenzdieb.common.BaseHelper
import ch.frequenzdieb.ticket.TicketType
import ch.frequenzdieb.ticket.TicketTypeHelper
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class LocationHelper(
    mongoTemplate: MongoTemplate,
    private val ticketTypeHandler: TicketTypeHelper
) : BaseHelper(mongoTemplate, Location::class.java) {
    fun createLocation(
        name: String = createRandomString(10),
        ticketTypes: List<TicketType> = ticketTypeHandler.createTicketType(2).insert()
    ) = Location(
        name = name,
        ticketTypeIds = ticketTypes.map { it.id!! }
    )
}

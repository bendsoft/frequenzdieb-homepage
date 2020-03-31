package ch.frequenzdieb.api.services.ticketing

import ch.frequenzdieb.api.BaseHelper
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class TicketingHelper(
    mongoTemplate: MongoTemplate
) : BaseHelper(mongoTemplate, Ticket::class.java) {
}

package ch.frequenzdieb.event.concert

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.event.location.Location
import ch.frequenzdieb.event.location.LocationHelper
import ch.frequenzdieb.ticket.TicketType
import ch.frequenzdieb.ticket.TicketTypeHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@AutoConfigureDataMongo
internal class ConcertHelper {
    @Autowired lateinit var locationHelper: LocationHelper
    @Autowired lateinit var ticketTypeHelper: TicketTypeHelper

    suspend fun createConcert(
        concertName: String = createRandomString(10),
        location: Location? = null,
        ticketTypes: List<TicketType>? = null,
        date: LocalDateTime = LocalDateTime.now().plusYears(1),
        liveActs: List<String> = listOf(createRandomString(10)),
        terms: String = createRandomString(10)
    ) = Concert(
        liveActs = liveActs,
        name = concertName,
        date = date,
        location = location ?: locationHelper.createLocation().insert(),
        terms = terms,
        ticketTypes = ticketTypes ?: listOf(ticketTypeHelper.createTicketType().insert())
    )
}

package ch.frequenzdieb.event.concert

import ch.frequenzdieb.event.Event
import ch.frequenzdieb.event.location.Location
import ch.frequenzdieb.ticket.TicketType
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Size

@Document(collection = "events")
@TypeAlias("model.event.concert")
class Concert(
    @field:Size(min = 1, message = "INVALID_SIZE_OF_ACTS")
    val liveActs: List<@Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE") String>,
    name: String,
    date: LocalDateTime,
    location: Location,
    terms: String,
    ticketTypes: List<TicketType>
) : Event(
    name = name,
    date = date,
    terms = terms,
    location = location,
    ticketTypes = ticketTypes
)

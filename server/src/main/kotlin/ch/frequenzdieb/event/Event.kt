package ch.frequenzdieb.event

import ch.frequenzdieb.common.BaseEntity
import ch.frequenzdieb.event.location.Location
import ch.frequenzdieb.ticketing.TicketType
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Future
import javax.validation.constraints.Size

@Document(collection = "events")
open class Event(
    @Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    val name: String,

    @Future
    val date: LocalDateTime,

    val locationId: String,

    val ticketTypeIds: List<String>,

    val terms: String?
) : BaseEntity()

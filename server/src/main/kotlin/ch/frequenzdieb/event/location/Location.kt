package ch.frequenzdieb.event.location

import ch.frequenzdieb.common.BaseEntity
import ch.frequenzdieb.ticketing.TicketType
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.Size

@Document(collection = "locations")
@TypeAlias("model.location")
data class Location (
    @Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    val name: String,

    val ticketTypeIds: List<String> = emptyList()
) : BaseEntity()

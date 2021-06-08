package ch.frequenzdieb.event

import ch.frequenzdieb.common.MutableEntity
import ch.frequenzdieb.event.location.Location
import ch.frequenzdieb.ticket.TicketType
import ch.frequenzdieb.ticket.validation.Validateable
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Future
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@Document(collection = "events")
open class Event(
    @Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    val name: String,

    @Future
    val date: LocalDateTime,

    val terms: String = "",

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @NotEmpty
    val ticketTypes: List<TicketType>,

    override var validationRules: List<String> = listOf(),

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val location: Location
) : MutableEntity(), Validateable

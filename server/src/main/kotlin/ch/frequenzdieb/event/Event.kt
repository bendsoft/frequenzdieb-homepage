package ch.frequenzdieb.event

import ch.frequenzdieb.common.MutableEntity
import ch.frequenzdieb.event.location.Location
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Transient
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

    val terms: String?,

    val validationRules: List<String> = emptyList(),

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var location: Location? = null,

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var locationId: String? = null
) : MutableEntity()

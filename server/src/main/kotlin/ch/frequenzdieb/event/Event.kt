package ch.frequenzdieb.event

import ch.frequenzdieb.common.BaseEntity
import ch.frequenzdieb.event.location.Location
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Future
import javax.validation.constraints.Size

@Document(collection = "events")
abstract class Event(
    @Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    val name: String,

    @Future
    val date: LocalDateTime,

    @DBRef
    val location: Location,

    val terms: String?
) : BaseEntity()

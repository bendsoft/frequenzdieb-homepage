package ch.frequenzdieb.event

import ch.frequenzdieb.common.BaseEntity
import java.time.LocalDateTime
import javax.validation.constraints.Future
import javax.validation.constraints.Size

abstract class Event(
    @Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    val name: String,

    @Future
    val date: LocalDateTime,

    @Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    val location: String,

    val terms: String?
) : BaseEntity()

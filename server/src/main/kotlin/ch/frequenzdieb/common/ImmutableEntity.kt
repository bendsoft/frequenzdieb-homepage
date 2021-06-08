package ch.frequenzdieb.common

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import java.time.Instant
import java.util.*

abstract class ImmutableEntity(
    @Id
    var id: String = UUID.randomUUID().toString(),

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @CreatedDate
    var createdDate: Instant = Instant.now()
)

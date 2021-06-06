package ch.frequenzdieb.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

abstract class MutableEntity(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @LastModifiedDate
    var lastModifiedDate: Instant = Instant.now()
) : ImmutableEntity()

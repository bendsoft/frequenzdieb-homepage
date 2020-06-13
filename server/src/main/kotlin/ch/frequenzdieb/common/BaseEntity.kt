package ch.frequenzdieb.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

abstract class BaseEntity(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    var id: String? = null,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @CreatedDate
    var createdDate: Instant = Instant.now(),

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @LastModifiedDate
    var lastModifiedDate: Instant = Instant.now()
)

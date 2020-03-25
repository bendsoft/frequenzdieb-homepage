package ch.frequenzdieb.api.services.concert

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Future
import javax.validation.constraints.Size

@Document(collection = "concerts")
@TypeAlias("model.concert")
data class Concert(
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    val id: String? = null,

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String,

    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val date: LocalDateTime,

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val location: String
)

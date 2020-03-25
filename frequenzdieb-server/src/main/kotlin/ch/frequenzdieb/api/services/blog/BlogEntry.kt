package ch.frequenzdieb.api.services.blog

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Size

@Document(collection = "blogentries")
@TypeAlias("model.blogentry")
data class BlogEntry(
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    val id: String? = null,

    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    val title: String,

    val data: String,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createDate: LocalDateTime = LocalDateTime.now()
)

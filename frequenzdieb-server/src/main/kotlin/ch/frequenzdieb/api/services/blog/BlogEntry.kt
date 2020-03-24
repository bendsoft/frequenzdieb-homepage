package ch.frequenzdieb.api.services.blog

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Size

@Document(collection = "blogentries")
@TypeAlias("model.blogentry")
data class BlogEntry(
    @Id
    val id: String? = null,

    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    val title: String,

    val data: String,

    val createDate: LocalDateTime = LocalDateTime.now()
)

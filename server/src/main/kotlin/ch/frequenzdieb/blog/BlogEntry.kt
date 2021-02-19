package ch.frequenzdieb.blog

import ch.frequenzdieb.common.MutableEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.Size

@Document(collection = "blogentries")
@TypeAlias("model.blogentry")
data class BlogEntry(
    @field:Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    val title: String,

    @field:Size(min = 10, max = 500, message = "INVALID_INPUT_SIZE")
    val content: String
) : MutableEntity()

package ch.frequenzdieb.subscription

import ch.frequenzdieb.common.MutableEntity
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.Email
import javax.validation.constraints.Size

@Document(collection = "subscriptions")
@TypeAlias("model.subscription")
data class Subscription(
    @field:Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    val name: String,

    @field:Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    val surname: String,

    @field:Email(message = "INVALID_EMAIL")
    @field:Size(min = 2, max = 100, message = "INVALID_INPUT_SIZE")
    @Indexed(unique = true)
    val email: String,

    var isNewsletterAccepted: Boolean = false,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var isConfirmed: Boolean = false
) : MutableEntity()

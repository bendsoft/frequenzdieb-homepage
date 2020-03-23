package ch.frequenzdieb.api.services.subscription

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Size

@Document(collection = "subscriptions")
@TypeAlias("model.subscription")
data class Subscription(
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    val id: String? = null,

    @Size(min = 2, max = 100, message = "Firstname must be between 2 and 100 characters")
    val name: String,

    @Size(min = 2, max = 100, message = "Lastname must be between 2 and 100 characters")
    val surname: String,

    @Email(message = "Email should be valid")
    val email: String,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @PastOrPresent
    val registrationDate: LocalDateTime = LocalDateTime.now(),

    var isNewsletterAccepted: Boolean = false,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var isConfirmed: Boolean = false
)

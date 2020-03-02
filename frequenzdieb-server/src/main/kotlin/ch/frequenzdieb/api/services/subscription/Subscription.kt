package ch.frequenzdieb.api.services.subscription

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
    @Id
    val id: String? = null,

    @Size(min = 2, max = 100, message = "Firstname must be between 2 and 100 characters")
    val firstname: String,

    @Size(min = 2, max = 100, message = "Lastname must be between 2 and 100 characters")
    val lastname: String,

    @Email(message = "Email should be valid")
    val email: String,

    @PastOrPresent
    val registrationDate: LocalDateTime = LocalDateTime.now(),

    var isNewsletterAccepted: Boolean = false,

    var isConfirmed: Boolean = false
)

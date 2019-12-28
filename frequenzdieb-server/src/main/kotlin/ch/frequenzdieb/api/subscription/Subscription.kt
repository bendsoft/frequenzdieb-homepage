package ch.frequenzdieb.api.subscription

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.Size

@Document(collection = "subscriptions")
@TypeAlias("model.subscription")
data class Subscription(
    @Id
    val id: String,

    @Size(min = 2, max = 100, message = "Surname must be between 2 and 100 characters")
    val surname: String = "",

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String = "",

    @Email(message = "Email should be valid")
    val email: String = "",

    val registrationDate: LocalDateTime,

    var isConfirmed: Boolean = false
)

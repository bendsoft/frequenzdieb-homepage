package ch.frequenzdieb.api.subscription

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "subscriptions")
@TypeAlias("model.subscription")
data class Subscription(
    @Id val id: String? = null,
    val name: String = "",
    val email: String = "",
    val registrationDate: LocalDate
)

package ch.frequenzdieb.api.services.concert

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.PastOrPresent

@Document(collection = "signups")
@TypeAlias("model.signup")
data class SignUp(
    @Id val id: String,
    val concertId: String,
    val subscriptionId: String,
    @PastOrPresent
    val date: LocalDate,
    val message: String,
    @AssertTrue
    val acceptedConditions: Boolean = false
)

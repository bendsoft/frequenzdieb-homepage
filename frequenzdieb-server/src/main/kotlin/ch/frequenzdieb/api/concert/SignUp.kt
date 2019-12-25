package ch.frequenzdieb.api.concert

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Email
import javax.validation.constraints.Future
import javax.validation.constraints.Size

@Document(collection = "signups")
@TypeAlias("model.signup")
data class SignUp(
    @Id val id: String,
    val concertId: String,
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String = "",
    @Email(message = "Email should be valid")
    val email: String = "",
    @Future val date: LocalDate,
    val message: String,
    @AssertTrue val acceptedConditions: Boolean = false
)

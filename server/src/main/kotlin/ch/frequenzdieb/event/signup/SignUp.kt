package ch.frequenzdieb.event.signup

import ch.frequenzdieb.common.BaseEntity
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.subscription.Subscription
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Size

@Document(collection = "signups")
@TypeAlias("model.signup")
data class SignUp(
    val eventId: String,

    val subscriptionId: String,

    @field:Size(max = 250, message = "INVALID_INPUT_SIZE")
    val message: String?,

    @field:AssertTrue
    val acceptedConditions: Boolean = false
) : BaseEntity()

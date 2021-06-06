package ch.frequenzdieb.event.signup

import ch.frequenzdieb.common.ImmutableEntity
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.subscription.Subscription
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Size

@Document(collection = "signups")
@TypeAlias("model.signup")
data class SignUp(
    val event: Event,

    val subscription: Subscription,

    @field:Size(max = 250, message = "INVALID_INPUT_SIZE")
    val message: String?,

    @field:AssertTrue
    val acceptedConditions: Boolean = false
) : ImmutableEntity()

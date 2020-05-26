package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseEntity
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.subscription.Subscription
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.Min
import javax.validation.constraints.Size

@Document(collection = "ticket")
@TypeAlias("model.ticket")
data class Ticket(
	@DBRef
	val subscription: Subscription,

	@DBRef
	val event: Event,

	val type: TicketType,

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	var isValid: Boolean = true
) : BaseEntity()

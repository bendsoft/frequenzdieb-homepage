package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseEntity
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.subscription.Subscription
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "tickets")
@TypeAlias("model.ticket")
@CompoundIndexes(
	CompoundIndex(name = "event_subscription", def = "{'event.id' : 1, 'subscription.id': 1}")
)
data class Ticket(
	@DBRef
	val subscription: Subscription,

	@DBRef
	val event: Event,

	val type: TicketType,

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	var isValid: Boolean = true
) : BaseEntity()

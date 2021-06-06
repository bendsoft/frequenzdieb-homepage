package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.ImmutableEntity
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.subscription.Subscription
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "tickets")
@TypeAlias("model.ticket")
@CompoundIndexes(
	CompoundIndex(name = "event_subscription", def = "{'eventId' : 1, 'subscriptionId': 1}")
)
@JsonIgnoreProperties(value = ["createdDate"])
data class Ticket(
	val subscription: Subscription,

	val event: Event,

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	val type: TicketType
) : ImmutableEntity()

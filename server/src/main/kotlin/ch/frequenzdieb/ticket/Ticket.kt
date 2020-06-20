package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseEntity
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
data class Ticket(
	val subscriptionId: String,

	val eventId: String,

	val typeId: String,

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	var isValid: Boolean = true
) : BaseEntity()

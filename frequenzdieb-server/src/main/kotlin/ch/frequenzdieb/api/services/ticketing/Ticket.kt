package ch.frequenzdieb.api.services.ticketing

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "ticketing")
@TypeAlias("model.ticket")
data class Ticket(
	@Id val id: String?,
	val subscriptionId: String,
	val concertId: String,
	var qrCodeHash: String,
	var created: LocalDateTime?,
	var isValid: Boolean = false
)

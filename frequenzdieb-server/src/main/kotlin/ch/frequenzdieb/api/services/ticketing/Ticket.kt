package ch.frequenzdieb.api.services.ticketing

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "ticketing")
@TypeAlias("model.ticket")
data class Ticket(
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Id
	val id: String? = null,

	val subscriptionId: String,

	val concertId: String,

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	var qrCodeHash: String?,

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	var created: LocalDateTime?,

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	var isValid: Boolean = true
)

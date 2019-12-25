package ch.frequenzdieb.api.ticketing

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticketing")
@TypeAlias("model.ticket")
data class Ticket(
    @Id val id: String,
    val subscriptionId: String,
    val concertId: String,
    val qrCode: String,
    var isValid: Boolean
)

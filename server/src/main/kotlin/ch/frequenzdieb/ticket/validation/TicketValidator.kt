package ch.frequenzdieb.ticket.validation

import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticketValidator")
@TypeAlias("model.ticketValidator")
data class TicketValidator (
    val script: String
)

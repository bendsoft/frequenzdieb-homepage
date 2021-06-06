package ch.frequenzdieb.ticket.archive

import ch.frequenzdieb.common.ImmutableEntity
import ch.frequenzdieb.ticket.Ticket
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "ticketArchive")
@TypeAlias("model.ticketArchive")
data class ArchivedTicket (
    val ticket: Ticket,
    val invalidatedAt: Instant
) : ImmutableEntity()

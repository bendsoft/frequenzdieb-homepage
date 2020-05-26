package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticketType")
@TypeAlias("model.ticketType")
data class TicketType (
    @DBRef
    val attributes: List<TicketAttribute>
) : BaseEntity()

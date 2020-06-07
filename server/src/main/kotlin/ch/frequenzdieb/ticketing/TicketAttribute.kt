package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticketAttribute")
@TypeAlias("model.ticketAttribute")
data class TicketAttribute (
    val key: String,
    val value: String,
    val tag: String? = null,
    val archived: Boolean = false,
    val validationRules: List<Any> = emptyList() // TODO: For later implementation
) : BaseEntity()

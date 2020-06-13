package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticketTypes")
@TypeAlias("model.ticketType")
data class TicketType (
    val name: String,

    val attributeIds: List<String>,

    val validationRules: List<String> = emptyList()
) : BaseEntity()

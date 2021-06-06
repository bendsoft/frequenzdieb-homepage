package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.ImmutableEntity
import ch.frequenzdieb.ticket.validation.Validateable
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticketAttribute")
@TypeAlias("model.ticketAttribute")
data class TicketAttribute (
    val name: String,
    val tag: TicketAttributeTag? = null,
    val data: Map<String, Any> = mapOf(),
    override val validationRules: List<String> = listOf()
) : ImmutableEntity(), Validateable

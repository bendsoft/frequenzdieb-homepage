package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.ImmutableEntity
import ch.frequenzdieb.ticket.validation.Validateable
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticketAttribute")
@TypeAlias("model.ticketAttribute")
data class TicketAttribute (
    val name: String,
    val key: String,
    val text: String,
    val tag: String? = null,
    val isoLanguage: String = "de-CH",
    override val validationRules: MutableList<String> = mutableListOf(),
    val data: MutableMap<String, Any> = mutableMapOf()
) : ImmutableEntity(), Validateable

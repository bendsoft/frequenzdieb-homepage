package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.ImmutableEntity
import ch.frequenzdieb.common.LANGUAGE
import ch.frequenzdieb.ticket.validation.Validateable
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticketTypes")
@TypeAlias("model.ticketType")
data class TicketType(
    val name: String,

    val isoLanguage: LANGUAGE = LANGUAGE.DE_CH,

    override val validationRules: MutableList<String> = mutableListOf(),

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var attributes: List<TicketAttribute>? = null,

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var attributeIds: List<String>? = null
) : ImmutableEntity(), Validateable

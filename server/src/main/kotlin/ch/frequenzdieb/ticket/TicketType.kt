package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.ImmutableEntity
import ch.frequenzdieb.common.LANGUAGE
import ch.frequenzdieb.ticket.validation.Validateable
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.NotEmpty

@Document(collection = "ticketTypes")
@TypeAlias("model.ticketType")
data class TicketType(
    val name: String,

    val isoLanguage: LANGUAGE = LANGUAGE.DE_CH,

    override val validationRules: List<String> = listOf(),

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @NotEmpty
    var attributes: List<TicketAttribute> = listOf(),
) : ImmutableEntity(), Validateable

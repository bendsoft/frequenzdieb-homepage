package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseEntity
import ch.frequenzdieb.common.ValidationError
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticketTypes")
@TypeAlias("model.ticketType")
data class TicketType (
    val name: String,

    @DBRef
    val attributes: List<TicketAttribute>,

    val validationRules: List<String> = emptyList()
) : BaseEntity() {
    init {
        if (attributes.distinctBy { it.key }.size < attributes.size) {
            ValidationError("DUPLICATE_KEY", mapOf("reason" to "Cannot add attributes with the same key"))
                .throwAsServerResponse()
        }
    }
}

package ch.frequenzdieb.ticket

data class TicketAttributeTag (
    val name: String,
    val key: String,
    val text: String,
    val isoLanguage: String = "de-CH"
)

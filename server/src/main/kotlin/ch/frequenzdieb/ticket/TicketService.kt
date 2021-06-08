package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.TemplateParser
import ch.frequenzdieb.event.EventRepository
import ch.frequenzdieb.event.concert.Concert
import ch.frequenzdieb.subscription.SubscriptionRepository
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.html.*
import kotlinx.html.dom.create
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class TicketService(
	private val resourceLoader: ResourceLoader,
	private val eventRepository: EventRepository,
	private val subscriptionRepository: SubscriptionRepository,
	private val templateParser: TemplateParser
) {
	fun createQRCode(ticket: Ticket) =
		encoder(
			QRCode.from(encoder(ticket.id.toByteArray()))
				.withSize(200, 200)
				.to(ImageType.PNG)
				.stream()
				.toByteArray()
		)

	suspend fun createPDF(ticket: Ticket): ByteArrayResource {
		return templateParser.parseHTMLTemplate(
			resourceLoader.getResource("classpath:ticket_template.html").inputStream
		).run {
			listOf(
				addSubscriptionTags(ticket),
				addEventTags(ticket),
				addTypeTags(ticket)
			)
			.fold(mapOf(
				"TICKET_QR_CODE" to create.img {
					src = "data:image/png;base64,${createQRCode(ticket)}"
				}
			)) { acc, curr -> acc.plus(curr) }
			.let { templateParser.replaceMarkups(this, it) }
			.let { htmlTemplate ->
				ByteArrayResource(ByteArrayOutputStream().also {
					PdfRendererBuilder().apply {
						useFastMode()
						withW3cDocument(htmlTemplate, "")
						toStream(it)
						run()
					}
				}.toByteArray())
			}
		}
	}

	private suspend fun Document.addEventTags(ticket: Ticket): Map<String, Element> =
		eventRepository.findById(ticket.event.id)
			.awaitSingle()
			.let { event ->
				mapOf(
					"TICKET_TITLE" to create.h1 { +event.name },
					"TICKET_EVENT_INFO" to create.span {
						style = "margin-top: -20px;"
						br { +"Wo: ${event.location.name}" }
						br { +"Wann: ${event.date.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.GERMAN))}" }
					},
					"TICKET_FOOTER" to create.p {
						style = "font-size: 9px;"
						+event.terms
					},
					when (event) {
						is Concert ->
							"TICKET_SECONDARY_TITLE" to create.span {
								strong { +"Live Acts: " }
								ul {
									event.liveActs.forEach { li { +it } }
								}
							}
						else ->
							"TICKET_SECONDARY_TITLE" to create.span {
								+""
							}
					}
				)
			}

	private fun Document.addTypeTags(ticket: Ticket) =
		ticket.type.let { type ->
			type.attributes
				.filter { it.tag != null }
				.map {
					it.tag!!.name to create.p {
						+"${it.tag.key}: ${it.tag.text}"
					}
				}
				.plus("TICKET_TYPE" to create.p {
					+"Tickettyp: ${type.name}"
				})
				.toMap()
		}

	private suspend fun Document.addSubscriptionTags(ticket: Ticket): Map<String, Element> =
		subscriptionRepository.findById(ticket.subscription.id)
			.awaitSingle()
			.let {
				mapOf("TICKET_OWNER_INFO" to create.p {
					+"Dieses Ticket wurde erstellt für ${it.surname} ${it.name}"
				})
			}

	fun encoder(data: ByteArray): String = Base64.getEncoder().encodeToString(data)

	fun decoder(data: String): String = String(Base64.getDecoder().decode(data))
}

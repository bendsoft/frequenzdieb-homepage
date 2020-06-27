package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.common.TemplateParser
import ch.frequenzdieb.common.Validators.Companion.executeValidation
import ch.frequenzdieb.common.zipToPairWhen
import ch.frequenzdieb.event.EventRepository
import ch.frequenzdieb.event.concert.Concert
import ch.frequenzdieb.event.location.LocationRepository
import ch.frequenzdieb.subscription.SubscriptionRepository
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import kotlinx.html.br
import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.strong
import kotlinx.html.style
import kotlinx.html.ul
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.Element
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.zip
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale

@Service
class TicketService(
	private val resourceLoader: ResourceLoader,
	private val eventRepository: EventRepository,
	private val locationRepository: LocationRepository,
	private val ticketTypeRepository: TicketTypeRepository,
	private val ticketAttributeRepository: TicketAttributeRepository,
	private val subscriptionRepository: SubscriptionRepository,
	private val templateParser: TemplateParser
) {
	fun createQRCode(ticket: Ticket) =
		ticket.id?.let {
			encoder(
				QRCode.from(encoder(it.toByteArray()))
					.withSize(200, 200)
					.to(ImageType.PNG)
					.stream()
					.toByteArray()
			)
		}.orEmpty()

	fun createPDF(ticket: Ticket): Mono<ByteArrayResource> {
		ticket.executeValidation(ErrorCode.TICKET_MISSING_SUBSCRIPTION) { ticket.subscriptionId.isNotEmpty() }

		return templateParser.parseHTMLTemplate(
			resourceLoader.getResource("classpath:ticket_template.html").inputStream
		).run {
			listOf(
				addSubscriptionTags(ticket),
				addTypeTags(ticket),
				addEventTags(ticket)
			).zip {
				it.fold(mapOf(
					"TICKET_QR_CODE" to
						create.img {
							src = "data:image/png;base64,${createQRCode(ticket)}"
						}
				)) { acc, curr -> acc.plus(curr) }
			}.map {
				templateParser.replaceMarkups(this, it)
			}.map { htmlTemplate ->
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

	private fun Document.addEventTags(ticket: Ticket): Mono<Map<String, Element>> =
		eventRepository.findById(ticket.eventId)
			.zipToPairWhen { locationRepository.findById(it.locationId) }
			.map { (event, location) ->
				val markupReplacements = mapOf(
					"TICKET_TITLE" to create.h1 { +event.name },
					"TICKET_EVENT_INFO" to create.span {
						style = "margin-top: -20px;"
						br { +"Wo: ${location.name}" }
						br { +"Wann: ${event.date.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.GERMAN))}" }
					}
				)

				event.terms?.let {
					markupReplacements.plus(
						"TICKET_FOOTER" to create.p {
							style = "font-size: 9px;"
							+it
						}
					)
				}

				when (event) {
					is Concert -> {
						markupReplacements.plus(
							"TICKET_SECONDARY_TITLE" to create.span {
								strong { +"Live Acts: " }
								ul {
									event.liveActs.forEach { li { +it } }
								}
							}
						)
					}
				}

				markupReplacements
			}

	private fun Document.addTypeTags(ticket: Ticket): Mono<Map<String, Element>> =
		ticketTypeRepository.findById(ticket.typeId)
			.zipToPairWhen { ticketAttributeRepository.findAllById(it.attributeIds).collectList() }
			.map { (type, attributes) ->
				val markupReplacements = mapOf("TICKET_TYPE" to create.p {
					+"Tickettyp: ${type.name}"
				})

				attributes
					.filter { !it.tag.isNullOrEmpty() }
					.forEach { attribute ->
						attribute.executeValidation(
							errorCode = ErrorCode.TICKET_DUPLICATE_TEMPLATE_TAG,
							errorDetails = arrayOf("reason" to "The tag \"${attribute.tag}\" has already been used")
						) { !markupReplacements.containsKey(it.tag) }

						markupReplacements.plus(attribute.tag!! to create.p {
							+"${attribute.key}: ${attribute.value}"
						})
					}

				markupReplacements
			}

	private fun Document.addSubscriptionTags(ticket: Ticket): Mono<Map<String, Element>> =
		subscriptionRepository.findById(ticket.subscriptionId)
			.map {
				mapOf("TICKET_OWNER_INFO" to create.p {
					+"Dieses Ticket wurde erstellt für ${it.surname} ${it.name}"
				})
			}

	fun encoder(data: ByteArray): String = Base64.getEncoder().encodeToString(data)

	fun decoder(data: String): String = String(Base64.getDecoder().decode(data))
}

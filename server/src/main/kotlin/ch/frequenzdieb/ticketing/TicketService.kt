package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.TemplateParser
import ch.frequenzdieb.common.ValidationError
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.event.EventRepository
import ch.frequenzdieb.event.concert.Concert
import ch.frequenzdieb.subscription.Subscription
import ch.frequenzdieb.subscription.SubscriptionRepository
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import kotlinx.coroutines.reactive.awaitFirstOrElse
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
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale

@Service
class TicketService(
	private val resourceLoader: ResourceLoader,
	private val eventRepository: EventRepository,
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
		if (ticket.subscription.id.isNullOrEmpty() || ticket.event.id.isNullOrEmpty()) {
			return Mono.empty()
		} else {
			return eventRepository.findById(ticket.event.id!!)
				.zipWhen { subscriptionRepository.findById(ticket.subscription.id!!) }
				.map {
					val event = it.t1
					val owner = it.t2

					templateParser.createHTMLTemplate(
						resourceLoader.getResource("classpath:ticket_template.html").inputStream
					) {
						createMarkupReplacementMap(event, ticket, owner)
					}.let { htmlTemplate ->
						ByteArrayResource(ByteArrayOutputStream().apply {
							use {
								PdfRendererBuilder().apply {
									useFastMode()
									withW3cDocument(htmlTemplate, "")
									toStream(it)
									run()
								}
							}
						}.toByteArray())
					}
				}
		}
	}

	private fun Document.createMarkupReplacementMap(
		event: Event,
		ticket: Ticket,
		owner: Subscription
	): MutableMap<String, Element> {
		val markupReplacements = mutableMapOf(
			"TICKET_TITLE" to create.h1 { +event.name },
			"TICKET_QR_CODE" to
				create.img {
					src = "data:image/png;base64,${createQRCode(ticket)}"
				},
			"TICKET_EVENT_INFO" to create.span {
				style = "margin-top: -20px;"
				br { +"Wo: ${event.location}" }
				br { +"Wann: ${event.date.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.GERMAN))}" }
			},
			"TICKET_OWNER_INFO" to create.p {
				+"Dieses Ticket wurde erstellt für ${owner.surname} ${owner.name}"
			}
		)

		ticket.type.attributes
			.filter { !it.tag.isNullOrEmpty() }
			.forEach {
				if (markupReplacements.containsKey(it.tag)) {
					ValidationError(
						"DUPLICATE_TICKET_TEMPLATE_TAG",
						mapOf("reason" to "The tag \"${it.tag}\" has already been used")
					).throwAsServerResponse()
				}

				markupReplacements[it.tag!!] = create.p {
					+"${it.key}: ${it.value}"
				}
			}

		event.terms?.let {
			markupReplacements.put(
				"TICKET_FOOTER",
				create.p {
					style = "font-size: 9px;"
					+event.terms
				}
			)
		}

		when (event) {
			is Concert -> {
				markupReplacements.putAll(mapOf(
					"TICKET_SECONDARY_TITLE" to
						create.span {
							strong { +"Live Acts: " }
							ul {
								event.liveActs.forEach { li { +it } }
							}
						}
				))
			}
		}

		return markupReplacements
	}

	fun encoder(data: ByteArray): String = Base64.getEncoder().encodeToString(data)

	fun decoder(data: String): String = String(Base64.getDecoder().decode(data))
}

package ch.frequenzdieb.api.services.ticketing

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

@Service
class TicketUtils {
	@Value("\${frequenzdieb.security.ticket.secret}")
	lateinit var ticketSecret: String

	@Autowired
	lateinit var resourceLoader: ResourceLoader

	fun createUniqueTicketHash(ticket: Ticket): String =
		DigestUtils.sha512Hex(ticket.subscriptionId + ticket.concertId + ticket.created + ticketSecret)

	fun createQRCode(ticket: Ticket) =
		encoder(
			QRCode.from(createUniqueTicketHash(ticket))
				.withSize(250, 250)
				.to(ImageType.PNG)
				.stream()
				.toByteArray()
		)

	fun createPDF(ticket: Ticket): ByteArrayResource {
		val htmlTemplate = DocumentBuilderFactory
			.newInstance()
			.newDocumentBuilder()
			.parse(resourceLoader.getResource("classpath:ticket_template.html").inputStream)
			.apply {
				normalize()
				getElementsByTagName("img")
					.apply {
						(0..this.length)
							.map { this.item(it) }
							.first { it.attributes.getNamedItem("id")?.nodeValue == "ticket-qr-code" }
							.let { it.attributes.getNamedItem("src").nodeValue = "data:image/png;base64,${createQRCode(ticket)}" }
					}
			}

		return ByteArrayResource(ByteArrayOutputStream().apply {
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

	private fun encoder(image: ByteArray): String {
		return Base64.getEncoder().encodeToString(image)
	}
}

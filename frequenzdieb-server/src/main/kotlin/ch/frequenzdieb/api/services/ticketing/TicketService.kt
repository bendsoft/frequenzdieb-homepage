package ch.frequenzdieb.api.services.ticketing

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

@Service
class TicketService {
	@Autowired
	lateinit var resourceLoader: ResourceLoader

	fun createQRCode(ticket: Ticket) =
		encoder(
			QRCode.from(encoder(ticket.id!!.toByteArray()))
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

	fun encoder(data: ByteArray): String = Base64.getEncoder().encodeToString(data)

	fun decoder(data: String): ByteArray = Base64.getDecoder().decode(data)
}

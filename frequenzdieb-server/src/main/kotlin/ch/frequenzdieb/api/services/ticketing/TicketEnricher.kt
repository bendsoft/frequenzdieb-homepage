package ch.frequenzdieb.api.services.ticketing

import com.mongodb.reactivestreams.client.gridfs.helpers.AsyncStreamHelper
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import org.apache.commons.codec.digest.DigestUtils
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

@Service
class TicketEnricher {
    @Value("\${FREQUENZDIEB_TICKET_SECRET}")
    lateinit var ticketSecret: String

    @Value("\${FREQUENZDIEB_PAYMENT_SECRET}")
    lateinit var paymentSecret: String

    @Autowired
    lateinit var gridFs: ReactiveGridFsTemplate

    @Autowired
    lateinit var resourceLoader: ResourceLoader

    inner class TicketHelpers(
        private val ticket: Ticket
    ) {
        private var pdfTicketOutputStream: ByteArrayOutputStream? = null
        private val qrCode = encoder(
            QRCode.from(DigestUtils.sha512Hex(ticket.id + ticketSecret))
                .withSize(250,250)
                .to(ImageType.PNG)
                .stream()
                .toByteArray()
        )

        init {
            requireNotNull(ticket.id)
        }

        fun createQRCode(): TicketHelpers {
            ticket.qrCode = qrCode
            ticket.paymentTransactionRefHash = DigestUtils.sha512Hex(ticket.id + paymentSecret)

            return this
        }

        fun createPDF(): TicketHelpers {
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
                                .let { it.attributes.getNamedItem("src").nodeValue = "data:image/png;base64,${qrCode}" }
                        }
                }

            pdfTicketOutputStream = ByteArrayOutputStream().apply {
                use {
                    PdfRendererBuilder().apply {
                        useFastMode()
                        withW3cDocument(htmlTemplate, "")
                        toStream(it)
                        run()
                    }
                }
            }

            return this
        }

        fun store(
            objectIdHandler: (fileId: ObjectId) -> Unit = { ticket.ticketFileId = it.toHexString() }
        ): Mono<Ticket> {
            requireNotNull(pdfTicketOutputStream)

            return gridFs
                .store(
                    AsyncStreamHelper.toAsyncInputStream(pdfTicketOutputStream?.toByteArray()),
                    "ticket_${ticket.id}.pdf",
                    APPLICATION_PDF.subtype,
                    null
                )
                .doOnNext { objectIdHandler(it) }
                .flatMap { Mono.just(ticket) }
        }

        private fun encoder(image: ByteArray): String {
            return Base64.getEncoder().encodeToString(image)
        }
    }

    fun with(ticket: Ticket): TicketHelpers {
        return TicketHelpers(ticket.copy())
    }
}

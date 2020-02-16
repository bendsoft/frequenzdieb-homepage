package ch.frequenzdieb.api.services.ticketing

import com.mongodb.reactivestreams.client.gridfs.helpers.AsyncStreamHelper
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

@Service
class TicketEnricher {
    @Autowired
    lateinit var gridFs: ReactiveGridFsTemplate

    @Autowired
    lateinit var resourceLoader: ResourceLoader

    inner class TicketHelpers(
        private val ticket: Ticket
    ) {
        private var pdfTicketOutputStream: ByteArrayOutputStream? = null
        private val qrCode = encoder(
            QRCode.from(ticket.id)
                .withSize(200,200)
                .to(ImageType.PNG)
                .stream()
                .toByteArray()
        )

        init {
            requireNotNull(ticket.id)
        }

        fun createQRCode(): TicketHelpers {
            ticket.qrCode = qrCode

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
        ): TicketHelpers {
            requireNotNull(pdfTicketOutputStream)

            gridFs
                .store(
                    AsyncStreamHelper.toAsyncInputStream(pdfTicketOutputStream?.toByteArray()),
                    "ticket_${ticket.id}.pdf",
                    APPLICATION_PDF.subtype,
                    null
                )
                .block()
                .let {
                    if (it != null) {
                        objectIdHandler(it)
                    }
                }

            return this
        }

        fun enrich(): Ticket {
            return ticket
        }

        private fun encoder(image: ByteArray): String {
            return Base64.getEncoder().encodeToString(image)
        }
    }

    fun with(ticket: Ticket): TicketHelpers {
        return TicketHelpers(ticket)
    }
}

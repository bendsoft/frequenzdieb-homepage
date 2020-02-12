package ch.frequenzdieb.api.services.ticketing

import com.mongodb.reactivestreams.client.gridfs.helpers.AsyncStreamHelper
import net.glxn.qrgen.javase.QRCode
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*


@Service
class TicketEnricher {
    @Autowired
    lateinit var gridFs: ReactiveGridFsTemplate

    class TicketHelpers(
        private val ticket: Ticket,
        private val gridFs: ReactiveGridFsTemplate
    ) {
        private val qrCode = QRCode.from(ticket.id).stream().toByteArray() //TODO: add property to config and load it with spring to have the base url

        fun createQRCode(): TicketHelpers {
            val qrCodeEncoded = encoder(qrCode)
            ticket.qrCode = qrCodeEncoded

            return this
        }

        fun createPDF(): TicketHelpers {
            val ticketDocument = PDDocument()
            val page = PDPage()
            ticketDocument.addPage(page)
            val contentStream = PDPageContentStream(ticketDocument, page)
            val image: PDImageXObject = PDImageXObject.createFromByteArray(ticketDocument, qrCode, "qr-code-ticket")
            contentStream.drawImage(image, 0f, 0f)
            contentStream.close()

            val baos = ByteArrayOutputStream()
            ticketDocument.save(baos)

            val fileId = gridFs.store(
                AsyncStreamHelper.toAsyncInputStream(baos.toByteArray()),
                "ticket_${ticket.id}.pdf",
                APPLICATION_PDF.subtype,
                null
            ).block()

            ticket.ticketFileId = fileId?.toHexString().orEmpty()
            ticketDocument.close()

            return this
        }

        fun enrich(): Ticket {
            return ticket
        }

        private fun encoder(imageBytes: ByteArray): String{
            return Base64.getEncoder().encodeToString(imageBytes)
        }
    }

    fun with(ticket: Ticket): TicketHelpers {
        return TicketHelpers(ticket, gridFs)
    }
}

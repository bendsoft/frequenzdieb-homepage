package ch.frequenzdieb.api.services.ticketing

import net.glxn.qrgen.javase.QRCode
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.stereotype.Service
import java.util.*

@Service
class TicketCreator {
    fun create(ticket: Ticket): Ticket {
        val qrCode = QRCode.from(ticket.id).stream().toByteArray() //TODO: add property to config and load it with spring to have the base url

        val qrCodeEncoded = encoder(qrCode)

        ticket.qrCode = qrCodeEncoded;

        val ticketDocument = PDDocument()
        val page = PDPage()
        ticketDocument.addPage(page)
        val contentStream = PDPageContentStream(ticketDocument, page)
        val image: PDImageXObject = PDImageXObject.createFromByteArray(ticketDocument, qrCode, "qr-code-ticket")
        contentStream.drawImage(image, 0f, 0f)
        contentStream.close()
        ticketDocument.save("ticket_${ticket.id}.pdf")
        ticketDocument.close()

        return ticket
    }

    private fun encoder(imageBytes: ByteArray): String{
        return Base64.getEncoder().encodeToString(imageBytes)
    }
}

package ch.frequenzdieb.api.services.ticketing

import ch.frequenzdieb.api.services.subscription.SubscriptionRepository
import ch.frequenzdieb.api.services.ticketing.payment.TransactionRepository
import ch.frequenzdieb.api.services.ticketing.payment.datatrans.DatatransHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class TicketingHandler {
    @Autowired lateinit var ticketingRepository: TicketingRepository
    @Autowired lateinit var ticketEnricher: TicketEnricher
    @Autowired lateinit var transactionRepository: TransactionRepository
    @Autowired lateinit var datatransHelper: DatatransHelper
    @Autowired lateinit var subscriptionRepository: SubscriptionRepository
    @Autowired lateinit var javaMailSender: JavaMailSender
    @Autowired lateinit var gridFs: ReactiveGridFsTemplate
    @Autowired lateinit var gridFsOperations: GridFsOperations

    @Value("\${frequenzdieb.mail.sender}")
    lateinit var senderEMailAddress: String

    fun findAllBySubscriptionId(req: ServerRequest) =
        Mono.justOrEmpty(req.queryParam("subscriptionid"))
            .filterWhen { subscriptionRepository.findById(it).hasElement() }
            .flatMap { ticketingRepository.findAllBySubscriptionId(it).collectList()
                .flatMap { tickets -> ok().bodyValue(tickets) }
                .switchIfEmpty(notFound().build())
            }
            .switchIfEmpty(badRequest().bodyValue("Please provide a valid subscriptionId"))

    fun create(req: ServerRequest) =
        req.bodyToMono(Ticket::class.java)
            .flatMap { ticketingRepository.insert(it) }
            .flatMap { createAndSafePDFTicket(it) }
            .doOnNext { sendTicketByEmail(it) }
            .flatMap {
                created(URI.create("/ticketing/${it.id}"))
                    .bodyValue(it)
            }
            .switchIfEmpty(badRequest().bodyValue("Ticket entity must be provided"))

    fun invalidate(req: ServerRequest) =
        ticketingRepository.findById(req.pathVariable("id"))
            .filter { it.isValid }
            .filterWhen { hasValidPaymentTransaction(it) }
            .doOnNext {
                it.isValid = false
                ticketingRepository.save(it)
            }
            .flatMap { ok().body(it, Ticket::class.java) }
            .switchIfEmpty(badRequest().bodyValue("Ticket is not valid"))

    fun downloadTicket (req: ServerRequest) =
        loadTicketFromDatabase(req.pathVariable("fileId"))
            .flatMap {
                ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .headers { httpHeaders ->
                        httpHeaders.setContentDispositionFormData(it.filename, it.filename)
                    }
                    .body(BodyInserters.fromResource(gridFsOperations.getResource(it)))
            }
            .switchIfEmpty(notFound().build())

    fun sendTicket (req: ServerRequest) =
        ticketingRepository.findById(req.pathVariable("id"))
            .doOnNext { sendTicketByEmail(it) }
            .flatMap { ok().build() }
            .switchIfEmpty(notFound().build())

    private fun sendTicketByEmail(ticket: Ticket) = GlobalScope.launch {
        loadTicketFromDatabase(ticket.ticketFileId)
            .zipWhen { subscriptionRepository.findById(ticket.subscriptionId) }
            .doOnSuccess {
                val mailMessage = javaMailSender.createMimeMessage()
                MimeMessageHelper(mailMessage, true).apply {
                    setFrom(senderEMailAddress)
                    setTo(it.t2.email)
                    setSubject("Test")
                    setText("Nur ein Test")
                    addAttachment(
                        it.t1.filename,
                        ByteArrayResource(gridFsOperations.getResource(it.t1).inputStream.readBytes())
                    )
                }

                javaMailSender.send(mailMessage)
            }.subscribe()
    }

    private fun hasValidPaymentTransaction(ticket: Ticket) =
        transactionRepository.findTopByUppTransactionIdAndSuccess_ResponseCode(ticket.paymentTransactionId)
            .switchIfEmpty(
                datatransHelper.getTransactionsByReference(ticket.id!!)
                    .next()
            )
            .filter { transaction -> datatransHelper.isTransactionSuccessful(transaction) }
            .hasElement()

    private fun createAndSafePDFTicket(ticket: Ticket) =
        ticketEnricher
            .with(ticket)
            .createQRCode()
            .createPDF()
            .store()

    private fun loadTicketFromDatabase(fileId: String) =
        gridFs.findOne(query(Criteria.where("_id").`is`(fileId)))
}

package ch.frequenzdieb.api.services.ticketing

import ch.frequenzdieb.api.services.subscription.SubscriptionRepository
import ch.frequenzdieb.api.services.ticketing.payment.TransactionRepository
import ch.frequenzdieb.api.services.ticketing.payment.datatrans.DatatransHelper
import com.mongodb.client.gridfs.model.GridFSFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class TicketingHandler {
    @Autowired
    lateinit var repository: TicketingRepository

    @Autowired
    lateinit var ticketEnricher: TicketEnricher

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var datatransHelper: DatatransHelper

    @Autowired
    lateinit var subscriptionRepository: SubscriptionRepository

    @Autowired
    lateinit var gridFs: ReactiveGridFsTemplate
    @Autowired
    lateinit var operations: GridFsOperations

    fun findAllBySubscriptionId(req: ServerRequest) =
        Mono.just(req.queryParam("subscriptionid"))
            .filter { it.isPresent }
            .map { it.get() }
            .filterWhen { subscriptionRepository.findById(it).hasElement() }
            .flatMap { repository.findAllBySubscriptionId(it).collectList()
                .flatMap { tickets -> ok().bodyValue(tickets) }
                .switchIfEmpty(notFound().build())
            }
            .switchIfEmpty(badRequest().bodyValue("Please provide a valid subscriptionId"))

    fun create(req: ServerRequest) =
        req.bodyToMono(Ticket::class.java)
            .flatMap { repository.insert(it)
                .doOnNext { ticket ->
                    ticketEnricher.with(ticket)
                        .createQRCode()
                        .createPDF()
                        .store()
                        .enrich()
                }
            }
            .flatMap { repository.save(it) }
            .flatMap {
                created(URI.create("/ticketing/${it.id}"))
                    .bodyValue(it)
            }
            .switchIfEmpty(badRequest().bodyValue("Ticket entity must be provided"))

    fun invalidate(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .filter { it.isValid }
            .filterWhen {
                transactionRepository.findTopByUppTransactionIdAndSuccess_ResponseCode(it.paymentTransactionId)
                    .switchIfEmpty(
                        datatransHelper.getTransactionsByReference(it.id!!)
                            .next()
                    )
                    .filter { transaction -> datatransHelper.isTransactionSuccessful(transaction) }
                    .hasElement()
            }
            .doOnNext {
                it.isValid = false
                repository.save(it)
            }
            .flatMap { ok().body(it, Ticket::class.java) }
            .switchIfEmpty(badRequest().bodyValue("Ticket is not valid"))

    fun downloadPDFTicket (req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .flatMap { loadTicketFromDatabase(it) }
            .flatMap {
                ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .headers { httpHeaders ->
                        httpHeaders.setContentDispositionFormData(it.filename, it.filename)
                    }
                    .body(BodyInserters.fromResource(operations.getResource(it)))
            }
            .switchIfEmpty(notFound().build())

    private fun loadTicketFromDatabase(ticket: Ticket): Mono<GridFSFile> {
        gridFs.delete(query(
            Criteria.where("_id")
                .`is`(ticket.ticketFileId)
        ))

        return ticketEnricher
            .with(ticket)
            .createQRCode()
            .createPDF()
            .store()
            .enrich()
            .let {
                gridFs.findOne(query(
                    Criteria.where("_id")
                        .`is`(it.ticketFileId)
                ))
            }
    }
}

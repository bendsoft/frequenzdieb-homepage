package ch.frequenzdieb.api.services.ticketing

import ch.frequenzdieb.api.services.subscription.SubscriptionRepository
import ch.frequenzdieb.api.services.ticketing.payment.TransactionRepository
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
            .flatMap { repository.insert(it) }
            .doOnNext { ticketEnricher.with(it)
                .createQRCode()
                .createPDF()
                .enrich()
            }
            .flatMap { repository.save(it) }
            .flatMap {
                created(URI.create("/ticketing/${it.id}"))
                    .bodyValue(it)
            }
            .switchIfEmpty(badRequest().bodyValue("Ticket entity must be provided"))

    fun invalidate(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .zipWhen { transactionRepository.findTopByUppTransactionId(it.paymentTransactionId) }
            .filter { it.t2.success?.responseCode == "01" } // TODO: Check if transaction is valid though, by asking datatrans
            .doOnNext {
                it.t1.isValid = false
                repository.save(it.t1)
            }
            .flatMap { ok().body(it, Ticket::class.java) }
            .switchIfEmpty(badRequest().bodyValue("Ticket is not valid"))

    fun downloadPDFTicket (req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .flatMap {
                println(it)
                gridFs.findOne(
                    query(
                        Criteria.where("_id")
                            .`is`(req.pathVariable(it.ticketFileId!!)) // TODO: Create file if not exists
                    )
                )
            }.flatMap {
                println(it)
                ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(BodyInserters.fromResource(operations.getResource(it)))
            }
}

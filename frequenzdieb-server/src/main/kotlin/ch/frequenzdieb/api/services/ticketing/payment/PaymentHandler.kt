package ch.frequenzdieb.api.services.ticketing.payment

import ch.frequenzdieb.api.services.ticketing.TicketingRepository
import ch.frequenzdieb.api.services.ticketing.payment.datatrans.UppTransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*

@Configuration
class PaymentHandler {
    @Autowired
    lateinit var repository: TicketingRepository

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    fun datatransWebhook(req: ServerRequest) =
        req.bodyToMono(UppTransactionService::class.java)
            .map { it.body.transaction }
            .doOnNext { transactionRepository.save(it) }
            .zipWhen { repository.findById(it.refno) }
            .flatMap {
                it.t2.paymentTransactionId = it.t1.uppTransactionId
                repository.save(it.t2)
                ok().build()
            }
            .switchIfEmpty(badRequest().bodyValue("Invalid Operation"))
}

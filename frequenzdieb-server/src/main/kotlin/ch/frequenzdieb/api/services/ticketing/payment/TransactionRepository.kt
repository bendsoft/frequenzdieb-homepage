package ch.frequenzdieb.api.services.ticketing.payment

import ch.frequenzdieb.api.services.ticketing.payment.datatrans.model.UppTransactionService
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TransactionRepository : ReactiveMongoRepository<UppTransactionService.Body.Transaction, String> {
    fun findTopByRefnoAndSuccess_ResponseCode(paymentRef: String, responseCode: String = "01"): Mono<UppTransactionService.Body.Transaction>
}

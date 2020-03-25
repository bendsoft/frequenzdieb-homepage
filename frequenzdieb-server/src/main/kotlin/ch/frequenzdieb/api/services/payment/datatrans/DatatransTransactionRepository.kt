package ch.frequenzdieb.api.services.payment.datatrans

import generated.UppTransactionService
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface DatatransTransactionRepository : ReactiveMongoRepository<UppTransactionService.Body.Transaction, String> {
    fun findTopByRefnoAndSuccess_ResponseCode(paymentRef: String, responseCode: String = "01"): Mono<UppTransactionService.Body.Transaction>
}

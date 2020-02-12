package ch.frequenzdieb.api.services.ticketing.payment

import ch.frequenzdieb.api.services.ticketing.payment.datatrans.UppTransactionService
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TransactionRepository : ReactiveMongoRepository<UppTransactionService.Body.Transaction, String> {
    fun findTopByUppTransactionId(subscriptionId: String): Mono<UppTransactionService.Body.Transaction>
}

package ch.frequenzdieb.payment.datatrans

import generated.UppTransactionService
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface DatatransTransactionRepository : ReactiveMongoRepository<UppTransactionService.Body.Transaction, String> {
    fun findTopByRefno(reference: String): Mono<UppTransactionService.Body.Transaction>
}

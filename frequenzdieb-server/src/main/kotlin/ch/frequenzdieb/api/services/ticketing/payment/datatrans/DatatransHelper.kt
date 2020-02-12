package ch.frequenzdieb.api.services.ticketing.payment.datatrans

import ch.frequenzdieb.api.services.ticketing.payment.datatrans.model.UppTransactionService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class DatatransHelper {
    fun getTransactionsByReference(reference: String): Flux<UppTransactionService.Body.Transaction> {
        return Flux.just(UppTransactionService.Body.Transaction())
    }

    fun isTransactionSuccessful(transaction: UppTransactionService.Body.Transaction) =
        transaction.success?.responseCode == "01"
}

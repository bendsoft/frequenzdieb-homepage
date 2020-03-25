package ch.frequenzdieb.api.services.payment

import reactor.core.publisher.Mono

interface PaymentService {
    fun createPaymentTransactionRefHash(id: String): String
    fun hasValidPaymentTransaction(id: String): Mono<Boolean>
}

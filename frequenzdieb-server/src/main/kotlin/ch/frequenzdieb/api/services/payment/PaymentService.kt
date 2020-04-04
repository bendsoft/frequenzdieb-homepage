package ch.frequenzdieb.api.services.payment

import reactor.core.publisher.Mono

interface PaymentService<T : Payment> {
    fun initiatePayment(request: T): T
    fun hasValidPaymentTransaction(reference: String): Mono<Boolean>
}

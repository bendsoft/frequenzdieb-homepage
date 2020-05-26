package ch.frequenzdieb.payment

import reactor.core.publisher.Mono

interface PaymentService<T : Payment> {
    fun initiatePayment(payment: T): Mono<out T>
    fun hasValidPayment(reference: String): Mono<Boolean>
}

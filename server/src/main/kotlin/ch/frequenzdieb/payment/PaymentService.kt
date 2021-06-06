package ch.frequenzdieb.payment

interface PaymentService<T : Payment> {
    fun initiatePayment(payment: T): T
    suspend fun hasValidPayment(reference: String): Boolean
    suspend fun loadValidPayment(reference: String): Payment
}

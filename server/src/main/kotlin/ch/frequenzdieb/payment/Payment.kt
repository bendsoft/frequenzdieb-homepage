package ch.frequenzdieb.payment

interface Payment : Amount

data class SimplePayment(
    override val amount: Int = 0,
    override val currency: String = "None"
) : Payment

package ch.frequenzdieb.payment

data class Price(
    override val amount: Int,
    override val currency: String
) : Amount

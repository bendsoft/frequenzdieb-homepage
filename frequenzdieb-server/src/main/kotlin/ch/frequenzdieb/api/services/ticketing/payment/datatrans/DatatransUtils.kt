package ch.frequenzdieb.api.services.ticketing.payment.datatrans

import ch.frequenzdieb.api.services.ticketing.Ticket
import ch.frequenzdieb.api.services.ticketing.payment.datatrans.model.UppTransactionService
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class DatatransUtils {
	@Value("\${FREQUENZDIEB_PAYMENT_SECRET}")
	lateinit var paymentSecret: String

	fun isTransactionSuccessful(transaction: UppTransactionService.Body.Transaction) =
		transaction.success?.responseCode == "01"

	fun createPaymentTransactionRefHash(ticket: Ticket) =
		DigestUtils.sha512Hex(ticket.id + paymentSecret)
}

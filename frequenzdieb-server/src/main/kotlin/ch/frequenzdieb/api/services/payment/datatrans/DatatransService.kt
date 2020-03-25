package ch.frequenzdieb.api.services.payment.datatrans

import ch.frequenzdieb.api.services.payment.PaymentService
import generated.UppTransactionService
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DatatransService : PaymentService {
	@Value("\${frequenzdieb.security.payment.secret}")
	lateinit var paymentSecret: String

	@Autowired
	lateinit var transactionRepository: DatatransTransactionRepository

	private fun isTransactionSuccessful(transaction: UppTransactionService.Body.Transaction) =
		transaction.success?.responseCode == "01"

	override fun createPaymentTransactionRefHash(id: String) =
		DigestUtils.sha512Hex(id + paymentSecret)

	override fun hasValidPaymentTransaction(id: String) =
		Mono.justOrEmpty(createPaymentTransactionRefHash(id))
			.flatMap { transactionRepository.findTopByRefnoAndSuccess_ResponseCode(it) }
			.filter { isTransactionSuccessful(it) }
			.hasElement()
}

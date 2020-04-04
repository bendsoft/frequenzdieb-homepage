package ch.frequenzdieb.api.services.payment.datatrans

import ch.frequenzdieb.api.services.payment.PaymentService
import generated.UppTransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class DatatransService(
	@Value("\${frequenzdieb.payment.datatrans.secret}") private val paymentSecret: String,
	@Value("\${frequenzdieb.payment.datatrans.merchantId}") private val datatransMerchantId: String,
	@Autowired private val transactionRepository: DatatransTransactionRepository
) : PaymentService<DatatransPayment> {
	private val mac: Mac = Mac.getInstance("HmacSHA256").apply {
		init(SecretKeySpec(paymentSecret.toByteArray(), "RawBytes"))
	}

	override fun initiatePayment(request: DatatransPayment): DatatransPayment =
		request.copy().apply {
			merchantId = datatransMerchantId
			signature = createPaymentSignature(
				datatransMerchantId,
				request.amount,
				request.currency,
				request.reference
			)
		}

	override fun hasValidPaymentTransaction(reference: String) =
		transactionRepository.findTopByRefnoAndSuccess_ResponseCode(reference)
			.filter { isTransactionSuccessful(it) }
			.hasElement()

	private fun isTransactionSuccessful(transaction: UppTransactionService.Body.Transaction) =
		transaction.success?.responseCode == "01"

	fun createPaymentSignature(vararg valuesToSing: String): String =
		mac.doFinal(valuesToSing.joinToString().toByteArray()).toString()

	fun checkTransactionSignature(dtTransaction: UppTransactionService.Body) =
		createPaymentSignature(
			dtTransaction.merchantId,
			dtTransaction.transaction.amount,
			dtTransaction.transaction.currency,
			dtTransaction.transaction.refno
		) == getSignatureFromTransaction(dtTransaction, "sign")

	fun checkTransactionSignature2(dtTransaction: UppTransactionService.Body) =
		createPaymentSignature(
			dtTransaction.merchantId,
			dtTransaction.transaction.amount,
			dtTransaction.transaction.currency,
			dtTransaction.transaction.uppTransactionId
		) == getSignatureFromTransaction(dtTransaction, "sign2")

	private fun getSignatureFromTransaction(it: UppTransactionService.Body, fieldName: String) =
		it.transaction.userParameters.parameter.find { it.name == fieldName }?.value
}

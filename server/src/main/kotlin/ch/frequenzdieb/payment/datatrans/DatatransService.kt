package ch.frequenzdieb.payment.datatrans

import ch.frequenzdieb.payment.PaymentService
import ch.frequenzdieb.security.SignatureFactory
import generated.UppTransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DatatransService(
	@Value("\${payment.datatrans.merchantId}") private val datatransMerchantId: String,
	@Autowired private val transactionRepository: DatatransTransactionRepository,
	@Autowired private val signatureFactory: SignatureFactory
) : PaymentService<DatatransPayment> {
	override fun initiatePayment(payment: DatatransPayment) =
		Mono.justOrEmpty(payment)
			.filter { !it.reference.isNullOrEmpty() }
			.map {
				it.copy().apply {
					merchantId = datatransMerchantId
					signature = signatureFactory.createPaymentSignature(
						datatransMerchantId,
						it.amount,
						it.currency,
						it.reference!!
					)
				}
			}

	override fun hasValidPayment(reference: String) =
		transactionRepository.findTopByRefno(reference)
			.filter { isTransactionSuccessful(it) }
			.hasElement()

	private fun isTransactionSuccessful(transaction: UppTransactionService.Body.Transaction) =
		transaction.success?.responseCode == "01"

	fun checkTransactionSignature(dtTransaction: UppTransactionService.Body) =
		signatureFactory.createPaymentSignature(
			dtTransaction.merchantId,
			dtTransaction.transaction.amount,
			dtTransaction.transaction.currency,
			dtTransaction.transaction.refno
		) == getSignatureFromTransaction(dtTransaction, "sign")

	fun checkTransactionSignature2(dtTransaction: UppTransactionService.Body) =
		signatureFactory.createPaymentSignature(
			dtTransaction.merchantId,
			dtTransaction.transaction.amount,
			dtTransaction.transaction.currency,
			dtTransaction.transaction.uppTransactionId
		) == getSignatureFromTransaction(dtTransaction, "sign2")

	private fun getSignatureFromTransaction(it: UppTransactionService.Body, fieldName: String) =
		it.transaction.userParameters.parameter.find { it.name == fieldName }?.value
}

package ch.frequenzdieb.api.services.ticketing.payment

import ch.frequenzdieb.api.services.ticketing.payment.datatrans.model.UppTransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Configuration
class PaymentHandler {
	@Autowired
	lateinit var transactionRepository: TransactionRepository

	fun datatransWebhook(req: ServerRequest) =
		req.bodyToMono(UppTransactionService::class.java)
			.map { it.body.transaction }
			.doOnNext { transactionRepository.save(it) }
			.flatMap { ok().build() }
			.switchIfEmpty(badRequest().bodyValue("Invalid Operation"))
}

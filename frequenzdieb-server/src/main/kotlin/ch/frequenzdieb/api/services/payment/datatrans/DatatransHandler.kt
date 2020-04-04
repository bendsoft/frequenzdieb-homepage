package ch.frequenzdieb.api.services.payment.datatrans

import generated.UppTransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Configuration
class DatatransHandler {
	@Autowired
	lateinit var transactionRepository: DatatransTransactionRepository

	@Autowired
	lateinit var datatransService: DatatransService

	fun datatransWebhook(req: ServerRequest) =
		req.bodyToMono(UppTransactionService::class.java)
			.filter { datatransService.checkTransactionSignature(it.body) }
			.flatMap { transactionRepository.save(it.body.transaction) }
			.log()
			.flatMap { ok().build() }
			.switchIfEmpty(badRequest().bodyValue("Invalid Operation"))
}

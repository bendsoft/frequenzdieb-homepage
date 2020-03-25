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

	//TODO: Check for valid reference before saving to db
	fun datatransWebhook(req: ServerRequest) =
		req.bodyToMono(UppTransactionService::class.java)
			.map { it.body.transaction }
			.flatMap { transactionRepository.save(it) }
			.log()
			.flatMap { ok().build() }
			.switchIfEmpty(badRequest().bodyValue("Invalid Operation"))
}

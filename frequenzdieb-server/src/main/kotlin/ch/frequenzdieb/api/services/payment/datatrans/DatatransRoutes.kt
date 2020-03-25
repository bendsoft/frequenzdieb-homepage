package ch.frequenzdieb.api.services.payment.datatrans

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.web.reactive.function.server.router

@Configuration
class DatatransRoutes(
	private val datatransHandler: DatatransHandler
) {
	@Bean
	fun datatransRouter() = router {
		"/api/ticketing/payment".nest {
			accept(APPLICATION_XML).nest {
				POST("/datatrans/webhook", datatransHandler::datatransWebhook)
			}
		}
	}
}
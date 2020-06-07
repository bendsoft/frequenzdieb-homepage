package ch.frequenzdieb.payment.datatrans

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.web.reactive.function.server.router

const val paymentRoute = "/api/payment"

@Configuration
class DatatransRoutes(
	private val datatransHandler: DatatransHandler
) {
	@Bean
	fun datatransRouter() = router {
		paymentRoute.nest {
			accept(APPLICATION_XML).nest {
				POST("/datatrans", datatransHandler::datatransWebhook)
			}
		}
	}
}

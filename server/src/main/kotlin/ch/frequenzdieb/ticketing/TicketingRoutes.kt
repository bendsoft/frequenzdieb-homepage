package ch.frequenzdieb.ticketing

import ch.frequenzdieb.security.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.router

@Configuration
class TicketingRoutes(
	private val ticketingHandler: TicketingHandler
) {
	private val baseRoute = "/api/ticketing"

	@Bean
	fun ticketingRouter() = router {
		baseRoute.nest {
			accept(APPLICATION_JSON).nest {
				GET("/", ticketingHandler::findAllBySubscriptionId)
				POST("/", ticketingHandler::create)
				POST("/{id}/pay", ticketingHandler::createPaymentForTicket)
				PUT("/invalidate", ticketingHandler::invalidate)
				GET("/{id}/download", ticketingHandler::downloadTicket)
				GET("/{id}/send", ticketingHandler::sendTicket)
			}
		}
	}

	@Bean
	fun ticketingMatchers(): ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit = {
		pathMatchers(HttpMethod.GET, baseRoute).hasRole(Role.ADMIN.toString())
		pathMatchers(HttpMethod.PUT, "$baseRoute/invalidate").hasRole(Role.ADMIN.toString())
		pathMatchers(HttpMethod.POST, "$baseRoute/").hasRole(Role.USER.toString())
		pathMatchers(HttpMethod.POST, "$baseRoute/*/pay").permitAll()
	}
}

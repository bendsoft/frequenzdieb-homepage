package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.createDefaultRoutes
import ch.frequenzdieb.security.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.router

@Configuration
class TicketingRoutes(
	private val ticketingHandler: TicketingHandler,
	private val ticketAttributeRepository: TicketAttributeRepository,
	private val ticketTypeRepository: TicketTypeRepository
) {
	private val baseRoute = "/api/ticketing"

	@Bean
	fun ticketingRouter() = router {
		baseRoute.nest {
			accept(APPLICATION_JSON).nest {
				GET("/", ticketingHandler::findAllBySubscriptionIdAndEventId)
				POST("/", ticketingHandler::create)
				POST("/{id}/pay", ticketingHandler::createPaymentForTicket)
				PUT("/invalidate", ticketingHandler::invalidate)
				GET("/{id}/download", ticketingHandler::downloadTicket)
				GET("/{id}/send", ticketingHandler::sendTicket)
				"type".nest {
					createDefaultRoutes(ticketTypeRepository)
					"attribute".nest {
						createDefaultRoutes(ticketAttributeRepository)
					}
				}
			}
		}
	}

	@Bean
	fun ticketingMatchers(): ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit = {
		pathMatchers(HttpMethod.GET, "$baseRoute/type/**").permitAll()
		pathMatchers("$baseRoute/type/**").hasRole(Role.ADMIN.toString())

		pathMatchers(HttpMethod.GET, baseRoute).hasRole(Role.ADMIN.toString())
		pathMatchers(HttpMethod.PUT, "$baseRoute/invalidate").hasRole(Role.ADMIN.toString())
		pathMatchers(HttpMethod.POST, "$baseRoute/").hasAnyRole(Role.ADMIN.toString(), Role.USER.toString())
		pathMatchers(HttpMethod.POST, "$baseRoute/*/pay").permitAll()
	}
}

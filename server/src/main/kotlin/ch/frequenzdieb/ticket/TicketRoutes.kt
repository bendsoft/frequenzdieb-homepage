package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.DefaultHandlers.create
import ch.frequenzdieb.common.DefaultHandlers.delete
import ch.frequenzdieb.common.DefaultHandlers.getAll
import ch.frequenzdieb.common.DefaultHandlers.getById
import ch.frequenzdieb.common.DefaultHandlers.returnList
import ch.frequenzdieb.common.DefaultHandlers.update
import ch.frequenzdieb.security.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.router

const val ticketRoute = "/api/ticket"

@Configuration
class TicketingRoutes(
	private val ticketingHandler: TicketHandler,
	private val ticketTypeHandler: TicketTypeHandler,
	private val ticketAttributeRepository: TicketAttributeRepository,
	private val ticketTypeRepository: TicketTypeRepository
) {
	@Bean
	fun ticketingRouter() = router {
		ticketRoute.nest {
			accept(APPLICATION_JSON).nest {
				GET("/{id}", ticketingHandler::getById)
				GET("/", ticketingHandler::findAllBySubscriptionIdAndEventId)
				POST("/", ticketingHandler::create)
				POST("/{id}/pay", ticketingHandler::createPaymentForTicket)
				PUT("/invalidate", ticketingHandler::invalidate)
				GET("/{id}/download", ticketingHandler::downloadTicket)
				GET("/{id}/send", ticketingHandler::sendTicket)
				"type".nest {
					GET("/") { ticketTypeRepository.getAll() }
					GET("/{id}") { ticketTypeRepository.getById(it) }
					POST("/", ticketTypeHandler::create)
					PUT("/{id}") { ticketTypeRepository.update(it) }
					DELETE("/{id}") { ticketTypeRepository.delete(it) }
					"attribute".nest {
						GET("/{id}") { ticketAttributeRepository.getById(it) }
						POST("/") { ticketAttributeRepository.create(it) }
						GET("/") { request ->
							request.queryParam("key")
								.map { ticketAttributeRepository.getAllByKey(it).returnList() }
								.orElse(ticketAttributeRepository.getAll())
						}
						PUT("/{id}/archive") { ticketAttributeRepository.update(it) }
					}
				}
			}
		}
	}

	@Bean
	fun ticketingMatchers(): ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit = {
		// user or admin
		pathMatchers(HttpMethod.GET, "$ticketRoute/*/download")
			.hasAnyRole(Role.ADMIN.toString(), Role.USER.toString())
		pathMatchers(HttpMethod.GET, "$ticketRoute/*/send")
			.hasAnyRole(Role.ADMIN.toString(), Role.USER.toString())
		pathMatchers(HttpMethod.POST, "$ticketRoute/*/pay")
			.hasAnyRole(Role.ADMIN.toString(), Role.USER.toString())
		pathMatchers(HttpMethod.POST, ticketRoute)
			.hasAnyRole(Role.ADMIN.toString(), Role.USER.toString())

		// only admin
		pathMatchers(HttpMethod.PUT, "$ticketRoute/invalidate")
			.hasAnyRole(Role.ADMIN.toString())

		pathMatchers("$ticketRoute/type/**")
			.hasRole(Role.ADMIN.toString())
	}
}

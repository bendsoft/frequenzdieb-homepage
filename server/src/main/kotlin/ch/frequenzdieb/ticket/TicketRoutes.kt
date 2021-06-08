package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.DefaultHandlers.asServerResponse
import ch.frequenzdieb.common.DefaultHandlers.create
import ch.frequenzdieb.common.DefaultHandlers.delete
import ch.frequenzdieb.common.DefaultHandlers.getAll
import ch.frequenzdieb.common.DefaultHandlers.getById
import ch.frequenzdieb.common.DefaultHandlers.update
import ch.frequenzdieb.security.auth.Role
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.queryParamOrNull

const val ticketRoute = "/api/ticket"

@Configuration
class TicketingRoutes(
	private val ticketHandler: TicketHandler,
	private val ticketTypeHandler: TicketTypeHandler,
	private val ticketAttributeRepository: TicketAttributeRepository,
	private val ticketTypeRepository: TicketTypeRepository
) {
	@Bean
	fun ticketingRouter() = coRouter {
		ticketRoute.nest {
			accept(APPLICATION_JSON).nest {
				GET("/{id}", ticketHandler::getById)
				GET("/", ticketHandler::findAllBySubscriptionIdAndEventId)
				POST("/", ticketHandler::create)
				POST("/{id}/pay", ticketHandler::createPaymentForTicket)
				PUT("/invalidate", ticketHandler::invalidate)
				GET("/{id}/download", ticketHandler::downloadTicket)
				GET("/{id}/send", ticketHandler::sendTicket)
				"type".nest {
					GET("/") { ticketTypeRepository.getAll() }
					GET("/{id}") { ticketTypeRepository.getById(it) }
					POST("/", ticketTypeHandler::create)
					PUT("/{id}") { ticketTypeRepository.update(it) }
					DELETE("/{id}") { ticketTypeRepository.delete(it) }
					"attribute".nest {
						GET("/{id}") { ticketAttributeRepository.getById(it) }
						POST("/") { ticketAttributeRepository.create(it) }
						GET("/") {
							it.queryParamOrNull("name")
								.let { attributeName ->
									if (attributeName !== null)
										ticketAttributeRepository.getAllByName(attributeName)
											.asFlow()
											.toList()
											.asServerResponse()
									else ticketAttributeRepository.getAll()
								}
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

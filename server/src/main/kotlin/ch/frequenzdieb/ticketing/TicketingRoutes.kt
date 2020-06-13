package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.DefaultHandlers.create
import ch.frequenzdieb.common.DefaultHandlers.createDefaultRoutes
import ch.frequenzdieb.common.DefaultHandlers.delete
import ch.frequenzdieb.common.DefaultHandlers.getAll
import ch.frequenzdieb.common.DefaultHandlers.getById
import ch.frequenzdieb.common.DefaultHandlers.update
import ch.frequenzdieb.common.Validators.Companion.executeValidation
import ch.frequenzdieb.common.Validators.Companion.validateAsyncWith
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.zipToPairWhen
import ch.frequenzdieb.security.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import java.net.URI

const val ticketingRoute = "/api/ticketing"

@Configuration
class TicketingRoutes(
	private val ticketingHandler: TicketingHandler,
	private val ticketAttributeRepository: TicketAttributeRepository,
	private val ticketTypeRepository: TicketTypeRepository
) {
	@Bean
	fun ticketingRouter() = router {
		ticketingRoute.nest {
			accept(APPLICATION_JSON).nest {
				GET("/", ticketingHandler::findAllBySubscriptionIdAndEventId)
				POST("/", ticketingHandler::create)
				POST("/{id}/pay", ticketingHandler::createPaymentForTicket)
				PUT("/invalidate", ticketingHandler::invalidate)
				GET("/{id}/download", ticketingHandler::downloadTicket)
				GET("/{id}/send", ticketingHandler::sendTicket)
				"type".nest {
					GET("/") { ticketTypeRepository.getAll() }
					GET("/{id}") { ticketTypeRepository.getById(it) }
					POST("/") { request ->
						request.bodyToMono(TicketType::class.java).validateEntity()
							.validateAsyncWith(
								errorCode = "DUPLICATE_KEY",
								errorDetails = *arrayOf("reason" to "Cannot add attributes with the same key")
							) { ticketType ->
								ticketAttributeRepository.findAllById(ticketType.attributeIds)
									.collectList()
									.map { attributeIds ->
										attributeIds.distinctBy { it.key }.size == attributeIds.size
									}
							}
							.flatMap { ticketTypeRepository.insert(it) }
							.flatMap {
								ServerResponse.created(URI.create("${request.path()}/${it.id}"))
									.bodyValue(it)
							}
					}
					PUT("/{id}") { ticketTypeRepository.update(it) }
					DELETE("/{id}") { ticketTypeRepository.delete(it) }
					"attribute".nest {
						createDefaultRoutes(ticketAttributeRepository)
					}
				}
			}
		}
	}

	@Bean
	fun ticketingMatchers(): ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit = {
		pathMatchers(HttpMethod.GET, "$ticketingRoute/type/**").permitAll()
		pathMatchers("$ticketingRoute/type/**").hasRole(Role.ADMIN.toString())

		pathMatchers(HttpMethod.GET, ticketingRoute).hasRole(Role.ADMIN.toString())
		pathMatchers(HttpMethod.PUT, "$ticketingRoute/invalidate").hasRole(Role.ADMIN.toString())
		pathMatchers(HttpMethod.POST, "$ticketingRoute/").hasAnyRole(Role.ADMIN.toString(), Role.USER.toString())
		pathMatchers(HttpMethod.POST, "$ticketingRoute/*/pay").permitAll()
	}
}

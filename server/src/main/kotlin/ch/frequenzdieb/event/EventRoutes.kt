package ch.frequenzdieb.event

import ch.frequenzdieb.common.DefaultHandlers.create
import ch.frequenzdieb.common.DefaultHandlers.createDefaultRoutes
import ch.frequenzdieb.common.DefaultHandlers.delete
import ch.frequenzdieb.common.DefaultHandlers.getAll
import ch.frequenzdieb.common.DefaultHandlers.getById
import ch.frequenzdieb.common.DefaultHandlers.update
import ch.frequenzdieb.event.concert.ConcertHandler
import ch.frequenzdieb.event.signup.SignUpRepository
import ch.frequenzdieb.security.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.router

@Configuration
class EventRoutes(
    private val eventRepository: EventRepository,
    private val signUpRepository: SignUpRepository,
    private val concertHandler: ConcertHandler
) {
    private val baseRoute = "/api/event"

    @Bean
    fun eventRouter() = router {
        baseRoute.nest {
            accept(MediaType.APPLICATION_JSON).nest {
                createDefaultRoutes(eventRepository)
            }
            "/concert".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("/", concertHandler::findAll)
                    GET("/{id}") { eventRepository.getById(it) }
                    PUT("/{id}") { eventRepository.update(it) }
                    DELETE("/{id}") { eventRepository.delete(it) }
                    POST("/", concertHandler::create)
                }
            }
            "/{eventId}/signup".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("/") { signUpRepository.getAll() }
                    GET("/{id}") { signUpRepository.getById(it) }
                    POST("/") { signUpRepository.create(it) }
                    DELETE("/{id}") { signUpRepository.delete(it) }
                }
            }
        }
    }

    @Bean
    fun eventMatchers(): ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit = {
        pathMatchers(HttpMethod.GET, baseRoute).permitAll()
        pathMatchers(HttpMethod.GET, "$baseRoute/*").permitAll()
        pathMatchers("$baseRoute/**")
            .hasRole(Role.ADMIN.toString())

        pathMatchers(HttpMethod.GET, "$baseRoute/concert/*").permitAll()
        pathMatchers("$baseRoute/concert/**")
            .hasRole(Role.ADMIN.toString())

        pathMatchers(HttpMethod.POST, "$baseRoute/*/signup")
            .hasAnyRole(Role.ADMIN.toString(), Role.HUMAN.toString())
        pathMatchers(HttpMethod.DELETE, "$baseRoute/*/signup/{id}")
            .hasAnyRole(Role.ADMIN.toString(), Role.HUMAN.toString())
        pathMatchers( "$baseRoute/*/signup")
            .hasRole(Role.ADMIN.toString())
    }
}

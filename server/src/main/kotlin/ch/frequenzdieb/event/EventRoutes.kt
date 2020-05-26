package ch.frequenzdieb.event

import ch.frequenzdieb.event.concert.ConcertHandler
import ch.frequenzdieb.event.signup.SignUpHandler
import ch.frequenzdieb.security.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.router

@Configuration
class EventRoutes(
    private val eventHandler: EventHandler,
    private val signUpHandler: SignUpHandler,
    private val concertHandler: ConcertHandler
) {
    private val baseRoute = "/api/event"

    @Bean
    fun eventRouter() = router {
        baseRoute.nest {
            "/concert".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("/", concertHandler::findAll)
                    GET("/{id}", concertHandler::findById)
                    POST("/", concertHandler::create)
                }
            }
            "/{eventId}/signup".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("/", signUpHandler::findAll)
                    POST("/", signUpHandler::create)
                    DELETE("/", signUpHandler::deleteAllByEmail)
                }
            }
            accept(MediaType.APPLICATION_JSON).nest {
                GET("/", eventHandler::findAll)
                GET("/{id}", eventHandler::findById)
                DELETE("/{id}", eventHandler::deleteById)
            }
        }
    }

    @Bean
    fun eventMatchers(): ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit = {
        pathMatchers(HttpMethod.POST, "$baseRoute/concert").hasRole(Role.ADMIN.toString())
        pathMatchers(HttpMethod.DELETE, "$baseRoute/*").hasRole(Role.ADMIN.toString())
        pathMatchers(HttpMethod.GET, "$baseRoute/*/signup").hasRole(Role.ADMIN.toString())
        pathMatchers("$baseRoute/*/signup").hasAnyRole(Role.ADMIN.toString(), Role.HUMAN.toString())
    }
}

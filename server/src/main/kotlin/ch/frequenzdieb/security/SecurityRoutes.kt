package ch.frequenzdieb.security

import ch.frequenzdieb.security.auth.AuthHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.coRouter

const val securityRoute = "/api/security"

@Configuration
class SecurityRoutes (
    private val authHandler: AuthHandler
) {
    @Bean
    fun securityRouter() = coRouter {
        securityRoute.nest {
            accept(MediaType.APPLICATION_JSON).nest {
                POST("/auth/login", authHandler::login)
            }
        }
    }
}

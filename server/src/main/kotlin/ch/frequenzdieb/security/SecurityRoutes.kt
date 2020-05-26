package ch.frequenzdieb.security

import ch.frequenzdieb.security.auth.AuthHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class SecurityRoutes (
    private val authHandler: AuthHandler
) {
    @Bean
    fun securityRouter() = router {
        "/api/security".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                POST("/auth/login", authHandler::login)
            }
        }
    }
}

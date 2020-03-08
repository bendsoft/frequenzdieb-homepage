package ch.frequenzdieb.api.services.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class AuthRoutes (
    private val securityHandler: AuthHandler
) {
    @Bean
    fun securityRouter() = router {
        "/api/auth".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                POST("/login", securityHandler::login)
            }
        }
    }
}

package ch.frequenzdieb.api.services.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class SecurityRoutes (
    private val securityHandler: SecurityHandler
) {
    @Bean
    fun securityRouter() = router {
        "/api/security".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                POST("/login", securityHandler::login)
            }
        }
    }
}

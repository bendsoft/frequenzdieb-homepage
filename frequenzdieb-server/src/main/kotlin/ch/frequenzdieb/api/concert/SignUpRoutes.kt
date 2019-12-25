package ch.frequenzdieb.api.concert

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class SignUpRoutes(
    private val signUpHandler: SignUpHandler
) {
    @Bean
    fun signUpRouter() = router {
        "/api/concert/{concertId}".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                POST("/signup", signUpHandler::create)
            }
        }
    }
}

package ch.frequenzdieb.api.services.concert

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class ConcertRoutes(
    private val concertHandler: ConcertHandler
) {
    @Bean
    fun concertRouter() = router {
        "/api/concert".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                GET("/", concertHandler::findAll)
                GET("/{id}", concertHandler::findById)
                POST("/", concertHandler::create)
                DELETE("/{id}", concertHandler::delete)
            }
        }
    }
}

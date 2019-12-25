package ch.frequenzdieb.api.ticketing

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

@Configuration
class TicketingRoutes(
    private val ticketingHandler: TicketingHandler
) {
    @Bean
    fun ticketingRouter() = router {
        "/api/ticketing".nest {
            accept(APPLICATION_JSON).nest {
                GET("/{id}", ticketingHandler::findById)
                POST("/", ticketingHandler::create)
                PUT("/{id}/invalidate", ticketingHandler::invalidate)
            }
         }
    }
}

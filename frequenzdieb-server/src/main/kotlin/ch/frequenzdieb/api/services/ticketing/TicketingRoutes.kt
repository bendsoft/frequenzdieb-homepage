package ch.frequenzdieb.api.services.ticketing

import ch.frequenzdieb.api.services.ticketing.payment.PaymentHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.*
import org.springframework.web.reactive.function.server.router

@Configuration
class TicketingRoutes(
    private val ticketingHandler: TicketingHandler,
    private val paymentHandler: PaymentHandler
) {
    @Bean
    fun ticketingRouter() = router {
        "/api/ticketing".nest {
            accept(APPLICATION_JSON).nest {
                GET("/", ticketingHandler::findAllBySubscriptionId)
                POST("/", ticketingHandler::create)
                PUT("/{id}/invalidate", ticketingHandler::invalidate)
                GET("/{id}/download/pdf", ticketingHandler::downloadPDFTicket)
            }
            accept(APPLICATION_XML).nest {
                POST("/payment", paymentHandler::datatransWebhook)
            }
         }
    }
}

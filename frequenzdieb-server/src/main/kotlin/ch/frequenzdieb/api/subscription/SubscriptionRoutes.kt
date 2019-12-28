package ch.frequenzdieb.api.subscription

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

@Configuration
class SubscriptionRoutes(
    private val subscriptionHandler: SubscriptionHandler
) {
    @Bean
    fun newsletterRouter() = router {
        "/api/subscription".nest {
            accept(APPLICATION_JSON).nest {
                PUT("/confirm/{id}", subscriptionHandler::confirm)
                GET("/query", subscriptionHandler::findAllByEmail)
                POST("/", subscriptionHandler::create)
                DELETE("/", subscriptionHandler::deleteAllByEmail)
            }
         }
    }
}

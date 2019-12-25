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
                GET("/{id}", subscriptionHandler::findById)
                PUT("/confirm/{id}", subscriptionHandler::confirm)
                GET("/query", subscriptionHandler::findAllByQuery)
                POST("/", subscriptionHandler::create)
                DELETE("/query", subscriptionHandler::deleteByQuery)
            }
         }
    }
}

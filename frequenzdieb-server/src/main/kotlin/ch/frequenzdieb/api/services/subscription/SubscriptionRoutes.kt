package ch.frequenzdieb.api.services.subscription

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

@Configuration
class SubscriptionRoutes(
    private val subscriptionHandler: SubscriptionHandler
) {
    @Bean
    fun subscriptionRouter() = router {
        "/api/subscription".nest {
            accept(APPLICATION_JSON).nest {
                GET("/{id}/confirm", subscriptionHandler::confirm)
                GET("/", subscriptionHandler::findFirstByEmail)
                POST("/", subscriptionHandler::create)
                PUT("/", subscriptionHandler::update)
                GET("/{id}/newsletter/unsubscribe", subscriptionHandler::unsubscribe)
                GET("/{id}/newsletter/subscribe", subscriptionHandler::subscribe)
                DELETE("/", subscriptionHandler::deleteAllByEmail)
            }
         }
    }
}

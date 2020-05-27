package ch.frequenzdieb.subscription

import ch.frequenzdieb.common.getById
import ch.frequenzdieb.security.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.router

@Configuration
class SubscriptionRoutes(
    private val subscriptionHandler: SubscriptionHandler,
    private val subscriptionRepository: SubscriptionRepository
) {
    private val baseRoute = "/api/subscription"

    @Bean
    fun subscriptionRouter() = router {
        baseRoute.nest {
            accept(APPLICATION_JSON).nest {
                GET("/{id}/confirm", subscriptionHandler::confirmWithSignature)
                GET("/{id}/resend-confirmation", subscriptionHandler::resendConfirmation)
                GET("/", subscriptionHandler::findFirstByEmail)
                GET("/{id}", subscriptionRepository::getById)
                POST("/", subscriptionHandler::create)
                PUT("/", subscriptionHandler::update)
                DELETE("/", subscriptionHandler::deleteAllByEmail)
                GET("/remove", subscriptionHandler::sendSubscriptionDeletionEMail)
                DELETE("/{id}", subscriptionHandler::deleteWithSignature)
            }
         }
    }

    @Bean
    fun subscriptionMatchers(): ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit = {
        // Allow signed requests to all
        pathMatchers(HttpMethod.GET, "$baseRoute/{id}/confirm").permitAll()
        pathMatchers(HttpMethod.DELETE, "$baseRoute/{id}").permitAll()

        // Allow deletion only to admin
        pathMatchers(HttpMethod.DELETE, baseRoute).hasRole(Role.ADMIN.toString())

        // All others must at least be human
        pathMatchers("$baseRoute/**").hasAnyRole(
            Role.ADMIN.toString(),
            Role.USER.toString(),
            Role.HUMAN.toString()
        )
    }
}

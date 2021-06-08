package ch.frequenzdieb.subscription

import ch.frequenzdieb.common.DefaultHandlers.getById
import ch.frequenzdieb.security.auth.Role
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.coRouter

const val subscriptionRoute = "/api/subscription"

@Configuration
class SubscriptionRoutes(
    private val subscriptionHandler: SubscriptionHandler,
    private val subscriptionRepository: SubscriptionRepository
) {

    @Bean
    @RouterOperations(
        RouterOperation(path = "/api/{id}/confirm"),
        RouterOperation(path = "/api/{id}/resend-confirmation"),
        RouterOperation(path = "/")
    )
    fun subscriptionRouter() = coRouter {
        subscriptionRoute.nest {
            accept(APPLICATION_JSON).nest {
                GET("/{id}/confirm", subscriptionHandler::confirmWithSignature)
                GET("/{id}/resend-confirmation", subscriptionHandler::resendConfirmation)
                GET("/", subscriptionHandler::findFirstByEmail)
                GET("/{id}") { subscriptionRepository.getById(it) }
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
        pathMatchers(HttpMethod.GET, "$subscriptionRoute/{id}/confirm").permitAll()
        pathMatchers(HttpMethod.DELETE, "$subscriptionRoute/{id}").permitAll()

        // Allow deletion only to admin
        pathMatchers(HttpMethod.DELETE, subscriptionRoute)
            .hasRole(Role.ADMIN.toString())

        // All others must at least be human
        pathMatchers("$subscriptionRoute/**")
            .hasAnyRole(Role.ADMIN.toString(), Role.USER.toString(), Role.HUMAN.toString())
    }
}

package ch.frequenzdieb.api.services.blog

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

@Configuration
class BlogRoutes(
    private val subscriptionHandler: BlogHandler
) {
    @Bean
    fun blogRouter() = router {
        "/api/blog".nest {
            accept(APPLICATION_JSON).nest {
                GET("/", subscriptionHandler::findAll)
                POST("/", subscriptionHandler::create)
                DELETE("/{id}", subscriptionHandler::deleteById)
            }
         }
    }
}

package ch.frequenzdieb.blog

import ch.frequenzdieb.security.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.router

@Configuration
class BlogRoutes(
    private val blogHandler: BlogHandler
) {
    private val baseRoute = "/api/blog"

    @Bean
    fun blogRouter() = router {
        baseRoute.nest {
            accept(APPLICATION_JSON).nest {
                GET("/", blogHandler::findAll)
                POST("/", blogHandler::create)
                DELETE("/{id}", blogHandler::deleteById)
            }
         }
    }

    @Bean
    fun blogMatchers(): ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit = {
        pathMatchers("$baseRoute/**").hasRole(Role.ADMIN.toString())
    }
}

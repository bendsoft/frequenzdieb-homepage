package ch.frequenzdieb.event.location

import ch.frequenzdieb.common.DefaultHandlers.createDefaultRoutes
import ch.frequenzdieb.security.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.reactive.function.server.router

const val locationRoute = "/api/event/location"

@Configuration
class LocationRoutes(
    private val locationRepository: LocationRepository
) {
    @Bean
    fun locationRouter() = router {
        locationRoute.nest {
            accept(MediaType.APPLICATION_JSON).nest {
                createDefaultRoutes(locationRepository)
            }
        }
    }

    @Bean
    fun locationMatchers(): ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit = {
        pathMatchers(HttpMethod.GET, "$locationRoute/**").permitAll()
        pathMatchers("$locationRoute/**").hasRole(Role.ADMIN.toString())
    }
}

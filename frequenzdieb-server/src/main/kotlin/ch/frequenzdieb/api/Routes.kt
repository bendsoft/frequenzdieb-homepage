package ch.frequenzdieb.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router
import org.springframework.core.io.ClassPathResource

@Configuration
class Routes {
    @Bean
    fun router() = router {
        resources("/**", ClassPathResource("static/"))
    }
}

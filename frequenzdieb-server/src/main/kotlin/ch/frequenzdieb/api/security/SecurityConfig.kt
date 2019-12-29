package ch.frequenzdieb.api.security

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class SecurityConfig {
    init {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    }

    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService {
        val admin: UserDetails = User
            .withUsername("admin")
            .password(encoder().encode("password"))
            .roles("ADMIN")
            .build()

        val user: UserDetails = User
            .withUsername("user")
            .password(encoder().encode("password"))
            .roles("USER")
            .build()

        return MapReactiveUserDetailsService(admin, user)
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf()
            .disable()
            .authorizeExchange()
            .pathMatchers(HttpMethod.GET,"/api/ticketing/*").hasRole("USER")
            .pathMatchers(HttpMethod.PUT,"/api/ticketing/*/invalidate").hasRole("ADMIN")
            .pathMatchers(HttpMethod.DELETE,"/api/subscription").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/api/subscription/query").hasRole("ADMIN")
            .pathMatchers(HttpMethod.POST,"/api/concert").hasRole("ADMIN")
            .pathMatchers(HttpMethod.DELETE,"/api/concert/*").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/api/concert/*/signup/*").hasRole("ADMIN")
            .pathMatchers("/**").permitAll()
            .and()
            .httpBasic()
        return http.build()
    }

    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

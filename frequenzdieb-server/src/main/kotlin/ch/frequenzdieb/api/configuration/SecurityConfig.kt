package ch.frequenzdieb.api.configuration

import ch.frequenzdieb.api.services.auth.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class SecurityConfig {
    init {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    }

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Bean
    fun userDetailsService() =
        ReactiveUserDetailsService {
            accountRepository.findOneByUsername(it)
                .map { account ->
                    User
                        .withUsername(account.username)
                        .password(account.password)
                        .roles(account.role.toString())
                        .build()
                }
        }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf()
            .disable()
            .authorizeExchange()
            .pathMatchers("/**").permitAll()
            .pathMatchers(HttpMethod.GET,"/api/ticketing/*").hasRole("USER")
            .pathMatchers(HttpMethod.PUT,"/api/ticketing/*/invalidate").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/api/subscription").hasRole("ADMIN")
            .pathMatchers(HttpMethod.POST,"/api/concert").hasRole("ADMIN")
            .pathMatchers(HttpMethod.DELETE,"/api/concert/*").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/api/concert/*/signup").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/api/concert/*/signup/*").hasRole("ADMIN")
            .and()
            .httpBasic()
        return http.build()
    }

    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

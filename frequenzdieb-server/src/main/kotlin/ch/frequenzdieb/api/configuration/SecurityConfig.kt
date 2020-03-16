package ch.frequenzdieb.api.configuration

import ch.frequenzdieb.api.services.auth.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@EnableWebFluxSecurity
class SecurityConfig {
    init {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    }

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Autowired
    lateinit var securityContextRepository: SecurityContextRepository

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
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .exceptionHandling()
            .authenticationEntryPoint { exchange, _ -> Mono.from { exchange.response.statusCode = HttpStatus.UNAUTHORIZED } }
            .accessDeniedHandler { exchange, _ -> Mono.from { exchange.response.statusCode = HttpStatus.FORBIDDEN } }
            .and()
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange()
            .pathMatchers(HttpMethod.OPTIONS).permitAll()
            .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/ticketing/*").hasRole("USER")
            .pathMatchers(HttpMethod.PUT, "/api/ticketing/*/invalidate").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/api/subscription").hasRole("ADMIN")
            .pathMatchers(HttpMethod.POST, "/api/concert").hasRole("ADMIN")
            .pathMatchers(HttpMethod.DELETE, "/api/concert/*").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/api/concert/*/signup").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/api/concert/*/signup/*").hasRole("ADMIN")
            .pathMatchers("/**").permitAll()
            .anyExchange().authenticated()
            .and().build()

    @Bean
    fun encoder() = BCryptPasswordEncoder()
}

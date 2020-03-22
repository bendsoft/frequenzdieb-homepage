package ch.frequenzdieb.api.configuration

import ch.frequenzdieb.api.services.auth.Account
import ch.frequenzdieb.api.services.auth.AccountRepository
import ch.frequenzdieb.api.services.auth.Role
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class SecurityConfig {
    private val ADMIN = Role.ADMIN.toString()

    @Value("\${frequenzdieb.security.admin.password}")
    lateinit var adminPassword: String

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var bearerAuthenticationFilter: BearerAuthenticationFilter

    @Bean
    fun initializeAdminAccount() = CommandLineRunner {
        accountRepository.findOneByUsername("admin")
            .switchIfEmpty(
                accountRepository.insert(
                    Account(
                        id = null,
                        username = "admin",
                        role = Role.ADMIN,
                        password = encoder().encode(adminPassword)
                    )
                )
            )
            .subscribe()
    }

    @Bean
    fun userDetailsService() = ReactiveUserDetailsService {
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
            .authorizeExchange()
            .pathMatchers(HttpMethod.OPTIONS).permitAll()
            .pathMatchers("/api/auth/login").permitAll()
            .and().addFilterAt(bearerAuthenticationFilter, SecurityWebFiltersOrder.FIRST)
            .authorizeExchange()
            .pathMatchers(HttpMethod.GET, "/api/ticketing/*").hasRole(ADMIN)
            .pathMatchers(HttpMethod.PUT, "/api/ticketing/*/invalidate").hasRole(ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/subscription").hasRole(ADMIN)
            .pathMatchers(HttpMethod.POST, "/api/concert").hasRole(ADMIN)
            .pathMatchers(HttpMethod.DELETE, "/api/concert/*").hasRole(ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/concert/*/signup").hasRole(ADMIN)
            .pathMatchers(HttpMethod.GET, "/api/concert/*/signup/*").hasRole(ADMIN)
            .pathMatchers("/api/**").permitAll()
            .anyExchange().authenticated()
            .and()
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .build()

    @Bean
    fun encoder() = BCryptPasswordEncoder()
}

package ch.frequenzdieb.security.configuration

import ch.frequenzdieb.security.auth.Account
import ch.frequenzdieb.security.auth.AccountRepository
import ch.frequenzdieb.security.auth.Role
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
import org.springframework.web.cors.CorsConfiguration

@EnableWebFluxSecurity
class SecurityConfig (
    @Value("\${spring.profiles.active:}") val activeProfile: String,
    @Autowired val accountRepository: AccountRepository,
    @Autowired val bearerAuthenticationFilter: BearerAuthenticationFilter,
    @Autowired val recaptchaFilter: RecaptchaFilter,
    @Autowired val apiAuthorizeExchangeSpecs: List<ServerHttpSecurity.AuthorizeExchangeSpec.() -> Unit>
) {
    @Bean
    fun initializeDefaultAccounts(
        @Value("\${admin.password}") adminPassword: String,
        @Value("\${user.password}") userPassword: String
    ) = CommandLineRunner {
        findUserAndInsertIfMissing("admin", adminPassword, Role.ADMIN, Role.USER)
        findUserAndInsertIfMissing("user", userPassword, Role.USER)
    }

    private fun findUserAndInsertIfMissing(username: String, password: String, vararg role: Role) {
        accountRepository.findOneByUsername(username)
            .switchIfEmpty(accountRepository.insert(
                Account(
                    username = username,
                    role = role.asList(),
                    password = encoder().encode(password)
                )
            ))
            .filter { !encoder().matches(password, it.password) }
            .flatMap {
                accountRepository.save(it.copy(
                    password = encoder().encode(password)
                ))
            }
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
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .disableCorsWhenDevProfileActive()
            .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/api/security/auth/login").permitAll()
                .pathMatchers("/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui/**", "/swagger-ui.html", "/webjars/swagger-ui/**").permitAll()
            .and()
                .addFilterAt(bearerAuthenticationFilter, SecurityWebFiltersOrder.FIRST)
                .addFilterAt(recaptchaFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange()
                .apply {
                    apiAuthorizeExchangeSpecs.forEach { it() }
                }
                .pathMatchers("/api/**").permitAll()
            .and().build()

    private fun ServerHttpSecurity.disableCorsWhenDevProfileActive(): ServerHttpSecurity =
        apply {
            if (arrayListOf("dev", "localdev").contains(activeProfile)) {
                this.cors().configurationSource {
                    CorsConfiguration().apply {
                        allowCredentials = true
                        allowedHeaders = listOf(CorsConfiguration.ALL)
                        allowedMethods = listOf(CorsConfiguration.ALL)
                        allowedOrigins = listOf(CorsConfiguration.ALL)
                    }
                }
            }
        }

    @Bean
    fun encoder() = BCryptPasswordEncoder()
}

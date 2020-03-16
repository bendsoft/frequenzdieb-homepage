package ch.frequenzdieb.api.configuration

import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SecurityContextRepository(
    private val authenticationManager: AuthenticationManager
) : ServerSecurityContextRepository {
    override fun save(swe: ServerWebExchange, sc: SecurityContext): Mono<Void> {
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun load(swe: ServerWebExchange): Mono<SecurityContext> {
        val request: ServerHttpRequest = swe.request
        val authToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            .takeIf { !it.isNullOrEmpty() && it.startsWith("Bearer ") }
            .orEmpty()
            .substring(7)

        return if (authToken.isNotEmpty()) {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(authToken, authToken))
                .flatMap { authenticationManager ->
                    ReactiveSecurityContextHolder.getContext()
                        .doOnNext { context -> context.authentication = authenticationManager }
                }
        } else Mono.empty()
    }
}

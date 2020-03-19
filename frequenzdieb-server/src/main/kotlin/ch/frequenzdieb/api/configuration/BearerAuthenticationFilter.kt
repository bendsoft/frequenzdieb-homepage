package ch.frequenzdieb.api.configuration

import ch.frequenzdieb.api.services.auth.JwtTokenService
import ch.frequenzdieb.api.services.auth.Role
import io.jsonwebtoken.Claims
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class BearerAuthenticationFilter(
    authenticationManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService
) : AuthenticationWebFilter(authenticationManager) {
    init {
        setServerAuthenticationConverter { swe ->
            Mono.justOrEmpty(swe.request.headers.getFirst(HttpHeaders.AUTHORIZATION))
                .filter { hasBearerTokenInHeader(it) }
                .map { jwtTokenService.getAllClaimsFromToken(extractTokenFromHeader(it)) }
                .filter { isClaimValid(it) }
                .map { claims ->
                    UsernamePasswordAuthenticationToken(
                        jwtTokenService.getUsernameFromClaim(claims),
                        null,
                        getGrantedAuthoritiesFromClaims(claims)
                    )
                }
        }
    }

    private fun getGrantedAuthoritiesFromClaims(claims: Claims) =
        claims.get("role", List::class.java)
            .map { Role.valueOf(it as String) }
            .map { SimpleGrantedAuthority("ROLE_${it.name}") }

    private fun hasBearerTokenInHeader(it: String?) =
        !it.isNullOrEmpty() && it.startsWith("Bearer ")

    private fun extractTokenFromHeader(it: String) =
        it.substring(7)

    private fun isClaimValid(claims: Claims) =
        !jwtTokenService.isClaimExpired(claims)
}

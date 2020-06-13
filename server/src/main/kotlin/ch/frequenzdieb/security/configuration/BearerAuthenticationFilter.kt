package ch.frequenzdieb.security.configuration

import ch.frequenzdieb.common.Validators.Companion.executeValidation
import ch.frequenzdieb.security.auth.JwtTokenService
import ch.frequenzdieb.security.auth.Role
import io.jsonwebtoken.Claims
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
                .doOnError {
                    executeValidation(
                        errorCode = "UNAUTHORIZED",
                        httpStatus = HttpStatus.UNAUTHORIZED,
                        errorDetails = arrayOf("Reason" to it.localizedMessage)
                    ) { false }
                }
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

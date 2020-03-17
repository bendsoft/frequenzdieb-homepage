package ch.frequenzdieb.api.configuration

import ch.frequenzdieb.api.services.auth.JwtTokenService
import ch.frequenzdieb.api.services.auth.Role
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager(
    private val jwtTokenService: JwtTokenService
) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken: String = authentication.credentials.toString()
        val claims = jwtTokenService.getAllClaimsFromToken(authToken)

        return if (!jwtTokenService.isClaimExpired(claims)) {
            val rolesMap: List<*> = claims.get("role", List::class.java)
            val roles: List<Role> = rolesMap.map { Role.valueOf(it as String) }
            val auth = UsernamePasswordAuthenticationToken(
                jwtTokenService.getUsernameFromClaim(claims),
                null,
                roles.map { SimpleGrantedAuthority(it.name) }
            )
            Mono.just(auth)
        } else Mono.empty()
    }
}

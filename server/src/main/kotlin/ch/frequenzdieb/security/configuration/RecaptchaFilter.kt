package ch.frequenzdieb.security.configuration

import ch.frequenzdieb.security.auth.Role
import ch.frequenzdieb.security.recaptcha.RecaptchaService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RecaptchaFilter(
    authenticationManager: AuthenticationManager,
    private val recaptchaService: RecaptchaService
) : AuthenticationWebFilter(authenticationManager) {
    init {
        setServerAuthenticationConverter { swe ->
            Mono.justOrEmpty(swe.request.queryParams["recaptcha"]?.first())
                .filterWhen {
                    recaptchaService.recaptchaVerify(it)
                        .map { verificationResult -> verificationResult.success }
                }
                .flatMap {
                    val humanAuth = SimpleGrantedAuthority("ROLE_${Role.HUMAN}")
                    ReactiveSecurityContextHolder.getContext()
                        .filter { it.authentication.isAuthenticated }
                        .map {
                            UsernamePasswordAuthenticationToken(
                                it.authentication.principal,
                                null,
                                it.authentication.authorities.plus(humanAuth)
                            )
                        }
                        .switchIfEmpty(
                            Mono.just(UsernamePasswordAuthenticationToken(
                                "Human",
                                null,
                                listOf(humanAuth)
                            ))
                        )
                }
        }
    }
}

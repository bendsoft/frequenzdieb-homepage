package ch.frequenzdieb.api.configuration

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication) = Mono.just(authentication)
}

package ch.frequenzdieb.api.services.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Configuration
class SecurityHandler {
    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtTokenService: JwtTokenService

    fun login(req: ServerRequest) =
        req.bodyToMono(AuthenticationRequest::class.java)
            .flatMap { authRequest ->
                accountRepository.findOneByUsername(authRequest.username)
                    .filter { passwordEncoder.matches(authRequest.password, it.password) }
                    .map { hashMapOf("token" to jwtTokenService.generateToken(it)) }
                    .flatMap { ok().bodyValue(it) }
            }
            .switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED).build())
}

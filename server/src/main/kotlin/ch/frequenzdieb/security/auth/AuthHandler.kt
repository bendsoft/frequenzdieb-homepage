package ch.frequenzdieb.security.auth

import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Configuration
class AuthHandler {
    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtTokenService: JwtTokenService

    fun login(req: ServerRequest) =
        req.bodyToMono(AuthenticationRequest::class.java)
            .validateEntity()
            .flatMap { authRequest ->
                accountRepository.findOneByUsername(authRequest.username)
                    .validateWith(httpStatus = HttpStatus.UNAUTHORIZED) { passwordEncoder.matches(authRequest.password, it.password) }
                    .map { AuthenticationResult(jwtTokenService.generateToken(it)) }
                    .flatMap { ok().bodyValue(it) }
            }
}

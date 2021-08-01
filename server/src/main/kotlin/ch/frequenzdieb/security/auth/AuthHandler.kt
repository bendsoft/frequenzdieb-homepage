package ch.frequenzdieb.security.auth

import ch.frequenzdieb.common.DefaultHandlers.asServerResponse
import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.buildAndAwait

@Configuration
class AuthHandler {
    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtTokenService: JwtTokenService

    suspend fun login(req: ServerRequest): ServerResponse =
        req.awaitBody(AuthenticationRequest::class)
            .validateEntity()
            .let { authRequest ->
                accountRepository.findOneByUsername(authRequest.username)
                    .awaitSingleOrNull()
                    ?.let { account ->
                        account.validateWith(
                            errorCode = ErrorCode.NOT_AUTHORIZED,
                            httpStatus = HttpStatus.UNAUTHORIZED
                        ) { passwordEncoder.matches(authRequest.password, it.password) }

                        val token = jwtTokenService.generateToken(account)
                        AuthenticationResult(token).asServerResponse()
                    }
                    ?: ServerResponse.badRequest().buildAndAwait()
            }
}

package ch.frequenzdieb.security.auth

import javax.validation.constraints.Size

data class AuthenticationRequest (
    @field:Size(min = 2, max = 30)
    val username: String,

    @field:Size(min = 5, max = 30)
    val password: String
)

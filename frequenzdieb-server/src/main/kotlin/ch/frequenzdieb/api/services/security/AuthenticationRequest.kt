package ch.frequenzdieb.api.services.security

data class AuthenticationRequest (
    val username: String,
    val password: String
)

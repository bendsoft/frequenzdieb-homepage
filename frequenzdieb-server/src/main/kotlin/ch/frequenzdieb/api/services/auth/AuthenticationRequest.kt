package ch.frequenzdieb.api.services.auth

data class AuthenticationRequest (
    val username: String,
    val password: String
)

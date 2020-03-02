package ch.frequenzdieb.api.services.security

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "accounts")
@TypeAlias("model.account")
data class Account(
    @Id
    val id: String?,
    val username: String,
    val password: String,
    val role: Role
)

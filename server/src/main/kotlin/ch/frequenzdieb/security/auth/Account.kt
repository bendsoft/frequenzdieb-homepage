package ch.frequenzdieb.security.auth

import ch.frequenzdieb.common.BaseEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "accounts")
@TypeAlias("model.account")
data class Account(
    val username: String,
    val password: String,
    val role: List<Role>
) : BaseEntity()

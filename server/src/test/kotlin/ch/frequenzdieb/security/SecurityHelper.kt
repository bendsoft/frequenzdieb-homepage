package ch.frequenzdieb.security

import ch.frequenzdieb.common.BaseHelper
import ch.frequenzdieb.security.auth.Account
import ch.frequenzdieb.security.auth.AuthenticationRequest
import ch.frequenzdieb.security.auth.AuthenticationResult
import ch.frequenzdieb.security.auth.Role
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient


@Component
@AutoConfigureDataMongo
internal class SecurityHelper (
    mongoTemplate: MongoTemplate,
    private val restClient: WebTestClient
) : BaseHelper(mongoTemplate, Account::class.java) {
    private val encoder = BCryptPasswordEncoder()

    fun createAccount(
        username: String,
        password: String,
        roles: List<Role> = listOf(Role.USER)
    ) = Account(
        username = username,
        password = encoder.encode(password),
        role = roles
    )

    inner class AuthenticatedRestClient {
        fun getAuthenticatedAsAdmin() =
            mutateRestClient("fakeAdmin")

        fun getAuthenticatedAsUser()=
            mutateRestClient("fakeUser")

        fun getAuthenticatedAsHuman() =
            mutateRestClient("fakeHuman")

        private fun mutateRestClient(username: String) =
            object : JacksonAnnotationIntrospector() {
                override fun findPropertyAccess(m: Annotated?): JsonProperty.Access? {
                    val access = super.findPropertyAccess(m)
                    if (
                        access == JsonProperty.Access.READ_ONLY ||
                        access == JsonProperty.Access.WRITE_ONLY
                    ) {
                        return JsonProperty.Access.AUTO
                    }
                    return access
                }
            }.let {
                restClient
                    .mutate()
                    .codecs {
                        // TODO: find out how we can change the deserialization with json to ignore restrictions
                        //       on data-class-properties set with a jackson-annotation, since that might result
                        //       in wrong deserialization in unit-tests
                    }
                    .defaultHeader("Authorization", "Bearer ${createJwtFor(username)}")
                    .build()
            }
    }

    fun initAccountsForRestClient(): AuthenticatedRestClient {
        createAccount(
            username = "fakeUser",
            password = "password",
            roles = listOf(Role.USER)
        ).insert()
        createAccount(
            username = "fakeAdmin",
            password = "password",
            roles = listOf(Role.ADMIN)
        ).insert()
        createAccount(
            username = "fakeHuman",
            password = "password",
            roles = listOf(Role.HUMAN)
        ).insert()

        return AuthenticatedRestClient()
    }

    fun createJwtFor(username: String)=
        restClient
            .post().uri("$securityRoute/auth/login")
            .bodyValue(AuthenticationRequest(
                username = username,
                password = "password"
            ))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectBody(AuthenticationResult::class.java)
            .returnResult()
            .run {
                responseBody shouldNotBe null
                responseBody?.token shouldNotBe null
                responseBody?.token shouldNotBe ""
                responseBody?.token!!
            }

    fun getRestClientUnauthenticated() = restClient
}

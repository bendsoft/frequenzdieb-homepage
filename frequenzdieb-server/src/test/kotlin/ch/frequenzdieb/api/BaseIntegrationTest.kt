package ch.frequenzdieb.api

import ch.frequenzdieb.api.configuration.SecurityConfig
import ch.frequenzdieb.api.services.auth.AuthenticationRequest
import ch.frequenzdieb.api.services.auth.AuthenticationResult
import io.kotlintest.specs.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest
@ComponentScan(
    basePackages = ["ch.frequenzdieb.api"],
    includeFilters = [ComponentScan.Filter(type = FilterType.REGEX, pattern = [".*Helper"])]
)
@Import(value = [SecurityConfig::class])
internal abstract class BaseIntegrationTest : DescribeSpec() {
    @Autowired
    private lateinit var restClient: WebTestClient

    protected fun getRestClientAuthenticatedWithAdmin(): WebTestClient {
        val jwtToken = restClient
            .post().uri("/api/auth/login")
            .bodyValue(AuthenticationRequest(
                username = "admin",
                password = "password"
            ))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .returnResult(AuthenticationResult::class.java)

        return restClient
            .mutate()
            .defaultHeader("Authorization", "Bearer $jwtToken")
            .build()
    }

    protected fun getRestClientUnauthenticated() = restClient
}

package ch.frequenzdieb.api

import ch.frequenzdieb.api.configuration.SecurityConfig
import io.kotlintest.specs.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions

@WebFluxTest
@ComponentScan(
    basePackages = ["ch.frequenzdieb.api"],
    includeFilters = [ComponentScan.Filter(type = FilterType.REGEX, pattern = [".*Helper"])]
)
@Import(value = [SecurityConfig::class])
internal abstract class BaseIntegrationTest : DescribeSpec() {
    @Autowired
    private lateinit var restClient: WebTestClient

    protected fun getRestClientAuthenticatedWithAdmin() =
        restClient.mutate().filter(ExchangeFilterFunctions.basicAuthentication("admin", "password")).build()

    protected fun getRestClientAuthenticatedWithUser() =
        restClient.mutate().filter(ExchangeFilterFunctions.basicAuthentication("user", "password")).build()

    protected fun getRestClientUnauthenticated() = restClient
}

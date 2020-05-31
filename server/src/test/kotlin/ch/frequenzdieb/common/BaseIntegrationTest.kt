package ch.frequenzdieb.common

import ch.frequenzdieb.security.auth.AuthenticationRequest
import ch.frequenzdieb.security.auth.AuthenticationResult
import ch.frequenzdieb.security.configuration.SecurityConfig
import ch.frequenzdieb.ticketing.Ticket
import io.kotlintest.specs.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.test.web.reactive.server.WebTestClient
import java.io.InputStream
import javax.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender as JavaMailSender

@WebFluxTest
@ComponentScan(
    basePackages = ["ch.frequenzdieb"],
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

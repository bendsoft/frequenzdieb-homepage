package ch.frequenzdieb.common

import ch.frequenzdieb.security.configuration.SecurityConfig
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import

@WebFluxTest
@ComponentScan(
    basePackages = ["ch.frequenzdieb"],
    includeFilters = [ComponentScan.Filter(type = FilterType.REGEX, pattern = [".*Helper"])]
)
@Import(value = [SecurityConfig::class])
internal abstract class BaseIntegrationTest(
    body: DescribeSpec.() -> Unit
) : DescribeSpec(body)

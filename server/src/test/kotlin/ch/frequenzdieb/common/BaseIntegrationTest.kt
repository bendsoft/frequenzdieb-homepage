package ch.frequenzdieb.common

import ch.frequenzdieb.security.configuration.SecurityConfig
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import

@WebFluxTest
@ComponentScan(
    basePackages = ["ch.frequenzdieb"],
    includeFilters = [
        ComponentScan.Filter(type = FilterType.REGEX, pattern = [".*Helper"]),
        ComponentScan.Filter(type = FilterType.REGEX, pattern = [".*Service"])
    ]
)
@Import(value = [SecurityConfig::class])
internal abstract class BaseIntegrationTest {
    init {
        // This is only needed when running in IntelliJ and makes me sick!
        setIdeaIoUseFallback()
    }
}

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.spring.SpringAutowireConstructorExtension

object ProjectConfig : AbstractProjectConfig() {
    override fun extensions(): List<Extension> =
        listOf(SpringAutowireConstructorExtension)
}

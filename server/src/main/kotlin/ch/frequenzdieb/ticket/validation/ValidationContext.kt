package ch.frequenzdieb.ticket.validation

import ch.frequenzdieb.common.ValidationFailure
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import javax.script.ScriptEngineManager

open class ValidationContext {
    companion object {
        val scriptEngine = ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223JvmLocalScriptEngine
    }

    private val collectedErrors: MutableList<String> = mutableListOf()

    protected fun <T : ValidationContext> executeScript(validationContext: T, script: String) {
        println(validationContext.javaClass.packageName)
        println(validationContext.javaClass.canonicalName)

        scriptEngine.apply {
            put("context", validationContext)
            eval(
                """
                import ${validationContext.javaClass.packageName}.*
                
                with (bindings["context"] as ${validationContext.javaClass.canonicalName}) {
                    $script
                }
                """.trimIndent()
            )
        }
    }

    protected fun throwCollectedErrors() {
        collectedErrors
            .takeIf { it.isNotEmpty() }
            ?.let {
                ValidationFailure(
                    details = collectedErrors
                        .mapIndexed { i, value -> (i + 1).toString() to value }
                        .toMap()
                ).throwAsServerResponse()
            }
    }

    @ValidationDslMarker
    infix fun Boolean.raiseValidationError(error: String) {
        if (this)
            collectedErrors.add(error)
    }

    @ValidationDslMarker
    infix fun Boolean?.raiseValidationError(error: String) = this?.apply {
        this.raiseValidationError(error)
    }

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isLessThan(b: C) = compareTo(b) < 0

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isMoreThan(b: C) = compareTo(b) > 0

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isEqual(b: C) = compareTo(b) == 0

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isNotEqual(b: C) = compareTo(b) != 0

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isMoreOrEqual(b: C) = compareTo(b) >= 0

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isLessOrEqual(b: C) = compareTo(b) <= 0
}

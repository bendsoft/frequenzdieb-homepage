package ch.frequenzdieb.ticket.validation

import ch.frequenzdieb.common.ValidationError
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import java.util.function.Consumer
import javax.script.ScriptEngineManager

enum class On {
    Order,
    Invalidation
}

enum class Error {
    NO_MORE_TICKETS_AVAILABLE,
    TICKET_NOT_PAID
}

abstract class AbstractValidationDsl {
    protected lateinit var currentEntity: Validateable
    protected abstract val validationRules: Map<Validateable, List<String>>

    protected open fun Validateable.onValidationHook() {}

    companion object {
        val scriptEngine = ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223JvmLocalScriptEngine
    }

    private val collectedErrors: MutableList<String> = mutableListOf()

    @ValidationDslMarker
    fun validate() {
        validationRules.forEach { (entity, script) ->
            currentEntity = entity
            entity.onValidationHook()

            scriptEngine.apply {
                put("context", this@AbstractValidationDsl)
                eval("""
                    import ch.frequenzdieb.ticket.validation.*
                    
                    with (bindings["context"] as ${this@AbstractValidationDsl.javaClass.canonicalName}) {
                        $script
                    }
                    """.trimIndent())
            }
        }

        collectedErrors
            .takeIf { it.isNotEmpty() }
            ?.let {
                ValidationError(
                    details = collectedErrors
                        .mapIndexed { i, value -> (i+1).toString() to value }
                        .toMap()
                )
                    .throwAsServerResponse()
            }
    }

    @ValidationDslMarker
    infix fun Boolean.raiseError(error: String) {
        if (!this)
            collectedErrors.add(error)
    }

    @ValidationDslMarker
    infix fun Boolean.raiseError(code: Error) {
        this raiseError code.toString()
    }

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isLessThan(b: C) = compareTo(b) < 0

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isMoreThan(b: C) = compareTo(b) > 0

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isEqual(b: C) = compareTo(b) == 0

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isMoreOrEqual(b: C) = compareTo(b) >= 0

    @ValidationDslMarker
    infix fun <C : Comparable<C>> C.isLessOrEqual(b: C) = compareTo(b) <= 0

    @ValidationDslMarker
    infix fun <T> T.thenCheck(predicate: T.() -> Boolean): Boolean =
        predicate()

    @ValidationDslMarker
    infix fun <T, R> T.then(action: T.() -> R): R =
        action()
}

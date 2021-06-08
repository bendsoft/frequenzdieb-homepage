package ch.frequenzdieb.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

class BaseHelper {
    companion object Dsl {
        @DslMarker
        annotation class HelperDsl

        lateinit var mongoReactiveTemplate: ReactiveMongoTemplate

        @HelperDsl
        suspend fun <T : ImmutableEntity> T.insert(): T =
            mongoReactiveTemplate.insert(this)
                .awaitFirst()

        @HelperDsl
        suspend fun <T : ImmutableEntity> List<T>.insert(): List<T> =
            mongoReactiveTemplate.insertAll(this)
                .collectList()
                .awaitFirst()

        @HelperDsl
        suspend fun <T : ImmutableEntity> Flow<T>.insert(): List<T> = toList().insert()

        private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        @HelperDsl
        fun createRandomString(stringLength: Int) = (1..stringLength)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

        @HelperDsl
        suspend fun resetCollection(entityClass: Class<*>) =
            mongoReactiveTemplate
                .dropCollection(entityClass)
                .awaitFirstOrNull()
    }
}

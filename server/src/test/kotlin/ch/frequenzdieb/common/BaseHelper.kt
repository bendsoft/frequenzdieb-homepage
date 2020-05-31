package ch.frequenzdieb.common

import io.kotlintest.extensions.TestListener
import org.springframework.data.mongodb.core.MongoTemplate

abstract class BaseHelper(
    protected val mongoTemplate: MongoTemplate,
    private val entityClass: Class<*>
) : TestListener {
    init {
        Dsl.mongoTemplate = mongoTemplate
    }

    companion object Dsl {
        @DslMarker
        annotation class HelperDsl

        lateinit var mongoTemplate: MongoTemplate

        @HelperDsl
        fun <T : BaseEntity> T.insert(): T =
            mongoTemplate.insert(this)

        @HelperDsl
        fun <T : BaseEntity> List<T>.insert(): List<T> =
            this.map { it.insert() }
    }

    fun resetCollection() {
        mongoTemplate.dropCollection(entityClass)
    }

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    fun createRandomString(stringLength: Int) = (1..stringLength)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

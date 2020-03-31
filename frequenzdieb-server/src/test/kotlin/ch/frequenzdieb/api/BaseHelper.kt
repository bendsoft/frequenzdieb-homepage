package ch.frequenzdieb.api

import io.kotlintest.extensions.TestListener
import org.springframework.data.mongodb.core.MongoTemplate

abstract class BaseHelper(
    protected val mongoTemplate: MongoTemplate,
    private val entityClass: Class<*>
) : TestListener {
    protected val collectionName = mongoTemplate.getCollectionName(entityClass)

    fun resetCollection() {
        mongoTemplate.dropCollection(entityClass)
    }

    fun getAll(): MutableList<*> =
        mongoTemplate.findAll(entityClass, collectionName)

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    fun createRandomString(stringLength: Int) = (1..stringLength)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

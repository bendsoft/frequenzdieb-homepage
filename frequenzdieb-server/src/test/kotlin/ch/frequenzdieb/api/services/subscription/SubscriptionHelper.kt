package ch.frequenzdieb.api.services.subscription

import com.mongodb.BasicDBObjectBuilder
import io.kotlintest.extensions.TestListener
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@AutoConfigureDataMongo
internal class SubscriptionHelper(
    private val mongoTemplate: MongoTemplate
) : TestListener {
    private val subscriptionCollectionName: String = mongoTemplate.getCollectionName(Blog::class.java)

    fun insertSubscriptionForHansMuster() {
        val objectToSave = BasicDBObjectBuilder.start()
            .add("email", "hans.muster@example.com")
            .add("name", "Muster")
            .add("surname", "Hans")
            .add("registrationDate", LocalDateTime.now())
            .get()

        mongoTemplate.insert(objectToSave, subscriptionCollectionName)
    }

    fun resetCollection() {
        mongoTemplate.dropCollection(Blog::class.java)
    }

    fun getAllSubscriptions(): MutableList<Blog> =
        mongoTemplate.findAll(Blog::class.java, subscriptionCollectionName)
}

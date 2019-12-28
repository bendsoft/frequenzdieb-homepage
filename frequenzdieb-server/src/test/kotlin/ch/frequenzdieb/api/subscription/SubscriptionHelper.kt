package ch.frequenzdieb.api.subscription

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
    private val subscriptionCollectionName: String = mongoTemplate.getCollectionName(Subscription::class.java)

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
        mongoTemplate.dropCollection(Subscription::class.java)
    }

    fun getAllSubscriptions(): MutableList<Subscription> =
        mongoTemplate.findAll(Subscription::class.java, subscriptionCollectionName)
}

package ch.frequenzdieb.api.services.subscription

import ch.frequenzdieb.api.BaseHelper
import com.mongodb.BasicDBObjectBuilder
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class SubscriptionHelper(
    mongoTemplate: MongoTemplate
) : BaseHelper(mongoTemplate, Subscription::class.java) {
    fun insertSubscriptionForHans(subscriberName: String): String {
        val objectToSave = BasicDBObjectBuilder.start()
            .add("email", "hans.${subscriberName}@example.com")
            .add("name", subscriberName)
            .add("surname", "Hans")
            .get()

        val insertedObject = mongoTemplate.insert(objectToSave, collectionName)
        return insertedObject.toMap()["_id"].toString()
    }

    fun getAllSubscriptions(): MutableList<Subscription> =
        mongoTemplate.findAll(Subscription::class.java, collectionName)
}

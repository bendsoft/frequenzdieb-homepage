package ch.frequenzdieb.event.concert

import ch.frequenzdieb.api.BaseHelper
import com.mongodb.BasicDBObjectBuilder
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@AutoConfigureDataMongo
internal class ConcertHelper(
    mongoTemplate: MongoTemplate
) : BaseHelper(mongoTemplate, Concert::class.java) {

    fun insertConcert(
        concertName: String = createRandomString(10),
        location: String = createRandomString(10)
    ): String {
        val objectToSave = BasicDBObjectBuilder.start()
            .add("name", concertName)
            .add("location", location)
            .add("date", LocalDateTime.of(2050, 5, 2, 10, 0))
            .get()

        val insertedObject = mongoTemplate.insert(objectToSave, collectionName)
        return insertedObject.toMap()["_id"].toString()
    }
}

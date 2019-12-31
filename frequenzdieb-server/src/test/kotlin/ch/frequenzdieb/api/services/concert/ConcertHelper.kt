package ch.frequenzdieb.api.services.concert

import com.mongodb.BasicDBObjectBuilder
import io.kotlintest.extensions.TestListener
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@AutoConfigureDataMongo
internal class ConcertHelper(
    private val mongoTemplate: MongoTemplate
) : TestListener {
    private val concertCollectionName: String = mongoTemplate.getCollectionName(Concert::class.java)

    fun insertConcert(): String {
        val objectToSave = BasicDBObjectBuilder.start()
            .add("name", "SmmerComeBackXX")
            .add("location", "Tatooine")
            .add("date", LocalDateTime.of(2050, 5, 2, 10, 0))
            .get()

        val insertedObject = mongoTemplate.insert(objectToSave, concertCollectionName)
        return insertedObject.toMap()["_id"].toString()
    }

    fun resetCollection() {
        mongoTemplate.dropCollection(Concert::class.java)
    }

    fun getAllConcerts(): MutableList<Concert> =
        mongoTemplate.findAll(Concert::class.java, concertCollectionName)
}

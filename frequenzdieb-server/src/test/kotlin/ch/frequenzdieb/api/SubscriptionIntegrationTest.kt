package ch.frequenzdieb.api

import com.mongodb.BasicDBObjectBuilder
import com.mongodb.DBObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@DataMongoTest
@ExtendWith(SpringExtension::class)
class SubscriptionIntegrationTest {
    @Test
    fun insertSubscriptionIntoDB(@Autowired mongoTemplate: MongoTemplate) {
        val objectToSave = BasicDBObjectBuilder.start()
            .add("email", "hans.muster@example.com")
            .add("name", "Hans Muster")
            .add("registrationDate", LocalDateTime.now())
            .get()

        mongoTemplate.save(objectToSave, "subscriptions")

        assertThat(mongoTemplate.findAll(DBObject::class.java, "subscriptions")).extracting("email")
            .containsOnly("hans.muster@example.com")
    }
}

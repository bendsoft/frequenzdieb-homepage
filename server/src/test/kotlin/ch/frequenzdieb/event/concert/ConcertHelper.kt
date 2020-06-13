package ch.frequenzdieb.event.concert

import ch.frequenzdieb.common.BaseHelper
import ch.frequenzdieb.event.location.Location
import ch.frequenzdieb.event.location.LocationHelper
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@AutoConfigureDataMongo
internal class ConcertHelper(
    mongoTemplate: MongoTemplate,
    private val locationHelper: LocationHelper
) : BaseHelper(mongoTemplate, Concert::class.java) {
    fun createConcert(
        concertName: String = createRandomString(10),
        location: Location = locationHelper.createLocation().insert(),
        date: LocalDateTime = LocalDateTime.now().plusYears(1),
        liveActs: List<String> = listOf(createRandomString(10)),
        terms: String = createRandomString(10)
    )= Concert(
        liveActs = liveActs,
        name = concertName,
        date = date,
        locationId = location.id!!,
        terms = terms
    )
}

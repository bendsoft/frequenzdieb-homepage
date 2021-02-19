package ch.frequenzdieb.event.concert

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.event.location.Location
import ch.frequenzdieb.event.location.LocationHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@AutoConfigureDataMongo
internal class ConcertHelper {
    @Autowired lateinit var locationHelper: LocationHelper

    suspend fun createConcert(
        concertName: String = createRandomString(10),
        location: Location? = null,
        date: LocalDateTime = LocalDateTime.now().plusYears(1),
        liveActs: List<String> = listOf(createRandomString(10)),
        terms: String = createRandomString(10)
    ) = Concert(
        liveActs = liveActs,
        name = concertName,
        date = date,
        location = location ?: locationHelper.createLocation().insert(),
        terms = terms
    )
}

package ch.frequenzdieb.event.location

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class LocationHelper {
    fun createLocation(
        name: String = createRandomString(10)
    ) = Location(name)
}

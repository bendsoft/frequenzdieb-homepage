package ch.frequenzdieb.event.location

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LocationRepository : ReactiveMongoRepository<Location, String>

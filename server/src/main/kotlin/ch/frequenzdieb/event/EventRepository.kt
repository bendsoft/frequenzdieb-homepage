package ch.frequenzdieb.event

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository : ReactiveMongoRepository<Event, String>

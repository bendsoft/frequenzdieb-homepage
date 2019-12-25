package ch.frequenzdieb.api.concert

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ConcertRepository : ReactiveMongoRepository<Concert, String>

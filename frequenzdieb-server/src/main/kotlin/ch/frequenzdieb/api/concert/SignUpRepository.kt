package ch.frequenzdieb.api.concert

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface SignUpRepository : ReactiveMongoRepository<SignUp, String> {
    fun deleteAllByEmail(email: String): Mono<Void>
}

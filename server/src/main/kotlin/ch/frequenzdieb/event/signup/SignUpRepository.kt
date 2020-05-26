package ch.frequenzdieb.event.signup

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface SignUpRepository : ReactiveMongoRepository<SignUp, String> {
    fun deleteAllBySubscriptionId(subscriptionId: String): Mono<Long>
}

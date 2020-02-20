package ch.frequenzdieb.api.services.subscription

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface SubscriptionRepository : ReactiveMongoRepository<Subscription, String> {
    fun findFirstByEmail(email: String): Mono<Subscription>
    fun deleteAllByEmail(email: String): Mono<Long>
}

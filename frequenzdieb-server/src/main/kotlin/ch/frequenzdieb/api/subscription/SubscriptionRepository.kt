package ch.frequenzdieb.api.subscription

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SubscriptionRepository : ReactiveMongoRepository<Subscription, String> {
    fun findAllByEmail(email: String): Flux<Subscription>
    fun deleteAllByEmail(email: String): Mono<Void>
}

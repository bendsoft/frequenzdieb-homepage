package ch.frequenzdieb.api.services.subscription

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SubscriptionRepository : ReactiveMongoRepository<Blog, String> {
    fun findAllByEmail(email: String): Flux<Blog>
    fun deleteAllByEmail(email: String): Mono<Long>
}

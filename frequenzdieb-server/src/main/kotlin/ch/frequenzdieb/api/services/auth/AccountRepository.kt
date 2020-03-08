package ch.frequenzdieb.api.services.auth

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AccountRepository : ReactiveMongoRepository<Account, String> {
    fun findOneByUsername(username: String): Mono<Account>
}

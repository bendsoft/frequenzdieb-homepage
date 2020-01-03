package ch.frequenzdieb.api.services.blog

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface BlogRepository : ReactiveMongoRepository<Blog, String>

package ch.frequenzdieb.blog

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BlogRepository : ReactiveMongoRepository<BlogEntry, String>

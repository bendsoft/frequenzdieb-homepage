package ch.frequenzdieb.ticket

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TicketAttributeRepository : ReactiveMongoRepository<TicketAttribute, String> {
    fun getAllByName(name: String): Flux<TicketAttribute>
    fun findAllByIdIn(attributeIds: List<String>): Flux<TicketAttribute>
}

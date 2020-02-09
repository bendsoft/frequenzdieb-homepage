package ch.frequenzdieb.api.services.ticketing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class TicketingHandler {
    @Autowired
    lateinit var repository: TicketingRepository

    @Autowired
    lateinit var ticketCreator: TicketCreator

    @Autowired
    lateinit var subscriptionRepository: TicketingRepository

    fun findAllBySubscriptionId(req: ServerRequest) =
        Mono.just(req.queryParam("subscriptionid"))
            .filter { it.isPresent }
            .map { it.get() }
            .filterWhen { subscriptionRepository.findById(it).hasElement() }
            .flatMap { repository.findAllBySubscriptionId(it).collectList()
                .flatMap { tickets -> ok().bodyValue(tickets) }
                .switchIfEmpty(notFound().build())
            }
            .switchIfEmpty(badRequest().bodyValue("Please provide a valid subscriptionId"))

    fun create(req: ServerRequest) =
        req.bodyToMono(Ticket::class.java)
            .doOnNext { ticketCreator.create(it) }
            .flatMap {
                repository.save(it)
                    .flatMap { ticket ->
                        created(URI.create("/ticketing/${ticket.id}"))
                            .bodyValue(ticket)
                    }
            }
            .switchIfEmpty(badRequest().bodyValue("Ticket entity must be provided"))

    fun invalidate(req: ServerRequest) =
        repository.findById(req.pathVariable("id"))
            .doOnNext {
                it.isValid = false
                repository.save(it)
            }
            .flatMap { ok().body(it, Ticket::class.java) }
}

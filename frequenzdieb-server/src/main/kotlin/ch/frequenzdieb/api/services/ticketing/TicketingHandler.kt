package ch.frequenzdieb.api.services.ticketing

import ch.frequenzdieb.api.services.subscription.BlogRepository
import net.glxn.qrgen.javase.QRCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*

@Configuration
class TicketingHandler {
    @Autowired
    lateinit var repository: TicketingRepository

    @Autowired
    lateinit var subscriptionRepository: BlogRepository

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
            .flatMap {
                repository.save(it)
                    .doOnNext { ticket ->
                        ticket.qrCode = encoder(
                            QRCode.from(ticket.id).stream().toByteArray() //TODO: add property to config and load it with spring to have the base url
                        )
                    }
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

    private fun encoder(imageBytes: ByteArray): String{
        return Base64.getEncoder().encodeToString(imageBytes)
    }
}

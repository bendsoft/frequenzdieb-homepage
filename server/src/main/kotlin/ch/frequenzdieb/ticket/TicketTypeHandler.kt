package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.Validators.Companion.validateAsyncWith
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import java.net.URI

@Configuration
class TicketTypeHandler(
    private val ticketAttributeRepository: TicketAttributeRepository,
    private val ticketTypeRepository: TicketTypeRepository
) {
    fun create(req: ServerRequest) =
        req.bodyToMono(TicketType::class.java).validateEntity()
            .validateAsyncWith(
                errorCode = "DUPLICATE_KEY",
                errorDetails = *arrayOf("reason" to "Cannot add attributes with the same key")
            ) { ticketType ->
                ticketAttributeRepository.findAllById(ticketType.attributeIds)
                    .collectList()
                    .map { attributeIds ->
                        attributeIds.distinctBy { it.key }.size == attributeIds.size
                    }
            }
            .flatMap { ticketTypeRepository.insert(it) }
            .flatMap {
                ServerResponse.created(URI.create("${req.path()}/${it.id}"))
                    .bodyValue(it)
            }
}

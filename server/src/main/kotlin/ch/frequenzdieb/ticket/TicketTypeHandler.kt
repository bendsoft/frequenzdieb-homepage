package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.DefaultHandlers.create
import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import ch.frequenzdieb.common.zipToPairWhen
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono

@Configuration
class TicketTypeHandler(
    private val ticketAttributeRepository: TicketAttributeRepository,
    private val ticketTypeRepository: TicketTypeRepository
) {
    fun create(req: ServerRequest) =
        req.bodyToMono(TicketType::class.java).validateEntity()
            .flattenTicketType(ticketAttributeRepository)
            .flatMap { ticketTypeRepository.create(req.path(), it) }

    //TODO: Add update
}

fun Mono<TicketType>.flattenTicketType(ticketAttributeRepository: TicketAttributeRepository) =
    validateWith(ErrorCode.TICKET_TYPE_DUPLICATE_ATTRIBUTE_IDS) { ticketType ->
        ticketType.attributes.map { it.id }.run { distinct().size == size }
    }
    .zipToPairWhen { ticketType ->
        ticketAttributeRepository.findAllByIdIn(ticketType.attributes.map { it.id })
            .collectList()
    }
    .doOnNext { (ticketType, attributes) -> ticketType.attributes = attributes }
    .validateWith(ErrorCode.TICKET_TYPE_DUPLICATE_TEMPLATE_TAG) { (_, attributes) ->
        attributes
            .filter { it.tag != null }
            .run {
                distinctBy { it.tag }.size == size
            }
    }
    .map { (ticketType, _) -> ticketType}

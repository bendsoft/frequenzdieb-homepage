package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.DefaultHandlers.create
import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Configuration
class TicketTypeHandler(
    private val ticketAttributeRepository: TicketAttributeRepository,
    private val ticketTypeRepository: TicketTypeRepository
) {
    suspend fun create(req: ServerRequest) =
        req.awaitBody(TicketType::class).validateEntity()
            .flattenTicketType(ticketAttributeRepository)
            .let {
                ticketTypeRepository.create(req.path(), it)
            }

    //TODO: Add update
}

suspend fun TicketType.flattenTicketType(ticketAttributeRepository: TicketAttributeRepository): TicketType =
    validateWith(ErrorCode.TICKET_TYPE_DUPLICATE_ATTRIBUTE_IDS) {
        attributes
            .map { it.id }
            .run { distinct().size == size }
    }
    .apply {
        ticketAttributeRepository
            .findAllByIdIn(attributes.map { it.id })
            .asFlow()
            .toList()
            .let {
                attributes = it
            }
    }
    .validateWith(ErrorCode.TICKET_TYPE_DUPLICATE_TEMPLATE_TAG) {
        attributes
            .filter { it.tag != null }
            .run {
                distinctBy { it.tag }.size == size
            }
    }

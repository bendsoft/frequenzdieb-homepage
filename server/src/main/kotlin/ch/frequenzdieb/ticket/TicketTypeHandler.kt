package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import ch.frequenzdieb.common.zipToPairWhen
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
            .validateWith(ErrorCode.TICKET_TYPE_MISSING_ATTRIBUTES) {
                it.attributeIds != null
            }
            .validateWith(ErrorCode.TICKET_TYPE_DUPLICATE_ATTRIBUTE_IDS) {
                it.attributeIds!!.run { distinct().size == size }
            }
            .zipToPairWhen {
                ticketAttributeRepository.findAllByIdIn(it.attributeIds!!)
                    .collectList()
            }
            .validateWith(ErrorCode.TICKET_TYPE_MISSING_ATTRIBUTES) {
                (_, attributes) -> attributes.size > 0
            }
            .validateWith(ErrorCode.TICKET_TYPE_DUPLICATE_TEMPLATE_TAG) {
                (_, attributes) -> attributes.distinctBy { it.tag }.size == attributes.size
            }
            .doOnNext { (type, attributes) -> type.attributes = attributes }
            .flatMap { (type, _) -> ticketTypeRepository.insert(type) }
            .flatMap {
                ServerResponse.created(URI.create("${req.path()}/${it.id}"))
                    .bodyValue(it)
            }
}

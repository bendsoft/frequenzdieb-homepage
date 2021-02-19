package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.DefaultHandlers.returnList
import ch.frequenzdieb.common.DefaultHandlers.returnOne
import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.common.RequestParamReader.readQueryParam
import ch.frequenzdieb.common.Validators.Companion.validateAsyncWith
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import ch.frequenzdieb.common.zipToPairWhen
import ch.frequenzdieb.email.EMailAttachment
import ch.frequenzdieb.email.EmailService
import ch.frequenzdieb.payment.PaymentService
import ch.frequenzdieb.payment.datatrans.DatatransPayment
import ch.frequenzdieb.subscription.SubscriptionRepository
import ch.frequenzdieb.ticket.archive.ArchivedTicket
import ch.frequenzdieb.ticket.archive.TicketArchiveRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.net.URI
import java.time.Instant
import java.util.logging.Level

@Configuration
class TicketHandler(
	private val ticketingRepository: TicketRepository,
	private val subscriptionRepository: SubscriptionRepository,
	private val ticketService: TicketService,
	private val ticketAttributeRepository: TicketAttributeRepository,
	private val ticketTypeRepository: TicketTypeRepository,
	private val ticketArchiveRepository: TicketArchiveRepository,
	private val paymentService: PaymentService<DatatransPayment>,
	private val emailService: EmailService
) {
	fun getById(req: ServerRequest) =
		ticketingRepository.findById(req.pathVariable("id"))
			.flattenTicket()
			.returnOne()

	fun findAllBySubscriptionIdAndEventId(req: ServerRequest) =
		ticketingRepository.findAllBySubscriptionIdAndEventId(
			req.readQueryParam("subscriptionId"),
			req.readQueryParam("eventId")
		)
			.doOnNext { it.flattenTicket() }
			.returnList()

	// TODO: Check if Tickets still can be bought -> check if event has already started
	fun create(req: ServerRequest) =
		req.bodyToMono(Ticket::class.java).validateEntity()
			.flattenTicket()
			.flatMap { ticketingRepository.insert(it) }
			.flatMap {
				created(URI.create("/ticketing/${it.id}"))
					.bodyValue(TicketCreateResponse(ticketService.createQRCode(it)))
			}

	fun createPaymentForTicket(req: ServerRequest) =
		req.bodyToMono(DatatransPayment::class.java).validateEntity()
			.validateAsyncWith(ErrorCode.TICKET_ID_INVALID)
				{ ticketingRepository.existsById(req.pathVariable("id")) }
			.flatMap { datatransPayment ->
				datatransPayment.reference = req.pathVariable("id")
				paymentService.initiatePayment(datatransPayment)
					.flatMap { ok().bodyValue(it) }
			}

	fun invalidate(req: ServerRequest) =
		req.bodyToMono(TicketInvalidationRequest::class.java).validateEntity()
			.zipToPairWhen {
				val ticketId = ticketService.decoder(it.qrCodeValue)
				ticketingRepository.findById(ticketId)
					.switchIfEmpty {
						ticketArchiveRepository.findByTicket_Id(ticketId)
							.validateWith(ErrorCode.TICKET_ALREADY_USED) { archivedTicket ->
								archivedTicket.invalidatedAt == null
							}
							.validateWith(ErrorCode.TICKET_ALREADY_ARCHIVED) { false }
							.log("ticket.invalidate", Level.WARNING)

						Mono.empty()
					}
			}
			.validateWith (ErrorCode.TICKET_FOR_ANOTHER_EVENT) {
				(invalidationRequest, ticket) -> ticket.eventId == invalidationRequest.eventId
			}
			.validateAsyncWith(ErrorCode.TICKET_NOT_PAID) {
				(_, ticket) -> paymentService.hasValidPayment(ticket.id!!)
			}
			.flatMap { (_, ticket) ->
				ticketArchiveRepository.insert(ArchivedTicket(
					ticket = ticket,
					invalidatedAt = Instant.now()
				))
					.doOnNext { ticketingRepository.deleteById(it.ticket.id!!) }
					.log("ticket.invalidate")
					.flatMap { ok().body(BodyInserters.fromValue(it)) }
			}
			.switchIfEmpty(notFound().build())

	fun downloadTicket(req: ServerRequest) =
		ticketingRepository.findById(req.pathVariable("id"))
			.zipToPairWhen { ticketService.createPDF(it) }
			.flatMap { (ticket, pdf) ->
				ok()
					.contentType(MediaType.APPLICATION_PDF)
					.headers { httpHeaders ->
						httpHeaders.setContentDispositionFormData(createTicketName(ticket), createTicketName(ticket))
					}
					.body(BodyInserters.fromResource(pdf))
			}
			.switchIfEmpty(notFound().build())

	fun sendTicket(req: ServerRequest) =
		ticketingRepository.findById(req.pathVariable("id"))
			.doOnNext { sendTicketByEmail(it) }
			.flatMap { ok().build() }
			.switchIfEmpty(notFound().build())

	private fun sendTicketByEmail(ticket: Ticket) = GlobalScope.launch {
		ticketService.createPDF(ticket)
			.zipToPairWhen { subscriptionRepository.findById(ticket.subscriptionId) }
			.doOnSuccess { (pdf, subscription) ->
				emailService.sendEmail(
					emailAddress = subscription.email,
					subject = "Dein Ticket zum Frequenzdieb-Konzert",
					message = "Anbei dein Ticket. Wir freuen uns auf einen tollen Abend mit dir!",
					attachment = EMailAttachment(
						attachmentFilename = "ticket_${ticket.id}.pdf",
						file = pdf
					)
				)
			}
			.subscribe()
	}

	private fun createTicketName(ticket: Ticket) = "ticket_${ticket.id}.pdf"

	private fun Mono<Ticket>.flattenTicket() =
		doOnNext { it.flattenTicket() }

	private fun Ticket.flattenTicket() =
		typeId?.let { id ->
			ticketTypeRepository.findById(id)
				.flattenTicketType()
				.doOnNext { type = it }
		}

	private fun Mono<TicketType>.flattenTicketType() =
		doOnNext { it.flattenTicketType() }

	private fun TicketType.flattenTicketType() =
		attributeIds?.let { ids ->
			ticketAttributeRepository.findAllByIdIn(ids)
				.collectList()
				.doOnNext { attributes = it }
		}
}

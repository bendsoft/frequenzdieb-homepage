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
import ch.frequenzdieb.event.EventRepository
import ch.frequenzdieb.payment.PaymentService
import ch.frequenzdieb.payment.datatrans.DatatransPayment
import ch.frequenzdieb.subscription.SubscriptionRepository
import ch.frequenzdieb.ticket.archive.ArchivedTicket
import ch.frequenzdieb.ticket.archive.TicketArchiveRepository
import ch.frequenzdieb.ticket.validation.validate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.net.URI
import java.time.Instant
import java.util.logging.Level

@Configuration
class TicketHandler(
	private val ticketRepository: TicketRepository,
	private val subscriptionRepository: SubscriptionRepository,
	private val ticketAttributeRepository: TicketAttributeRepository,
	private val ticketTypeRepository: TicketTypeRepository,
	private val eventRepository: EventRepository,
	private val ticketArchiveRepository: TicketArchiveRepository,
	private val ticketService: TicketService,
	private val paymentService: PaymentService<DatatransPayment>,
	private val emailService: EmailService
) {
	fun getById(req: ServerRequest) =
		ticketRepository.findById(req.pathVariable("id"))
			.flattenTicket()
			.returnOne()

	fun findAllBySubscriptionIdAndEventId(req: ServerRequest) =
		ticketRepository.findAllBySubscription_IdAndEvent_Id(
			req.readQueryParam("subscriptionId"),
			req.readQueryParam("eventId")
		)
			.doOnNext { it.flattenTicket() }
			.returnList()

	// TODO: Check if Tickets still can be bought -> check if event has already started
	// TODO: Check that subscriber in Payload matches User (must not be able to order in the name of another subscriber)
	fun create(req: ServerRequest) =
		req.bodyToMono(Ticket::class.java).validateEntity().flattenTicket()
			.doOnNext {
				runBlocking {
					it.validate(ticketRepository, ticketTypeRepository, paymentService).executeOnOrder()
				}
			}
			.flatMap { ticketRepository.insert(it) }
			.flatMap {
				created(URI.create("${ticketRoute}/${it.id}"))
					.bodyValue(TicketQrCode(ticketService.createQRCode(it)))
			}

	fun createPaymentForTicket(req: ServerRequest) =
		req.bodyToMono(DatatransPayment::class.java).validateEntity()
			.validateAsyncWith(ErrorCode.TICKET_ID_INVALID)
				{ ticketRepository.existsById(req.pathVariable("id")) }
			.flatMap { payment ->
				payment.reference = req.pathVariable("id")
				ok().bodyValue(paymentService.initiatePayment(payment))
			}

	fun invalidate(req: ServerRequest) =
		req.bodyToMono(TicketQrCode::class.java)
			.flatMap {
				val ticketId = ticketService.decoder(it.qrCode)
				ticketRepository.findById(ticketId)
					.switchIfEmpty {
						ticketArchiveRepository.findByTicket_Id(ticketId)
							.validateWith(ErrorCode.TICKET_ALREADY_USED) { archivedTicket ->
								archivedTicket.invalidatedAt.isBefore(Instant.now())
							}
							.validateWith(ErrorCode.TICKET_ALREADY_ARCHIVED) { false }
							.log("ticket.invalidate", Level.WARNING)

						Mono.empty()
					}
			}
			.validateAsyncWith(ErrorCode.TICKET_TYPE_FOR_EVENT_INVALID) { ticket ->
				eventRepository.findById(ticket.event.id!!)
					.map { it.ticketTypes.contains(ticket.type) }
			}
			.doOnNext { ticket ->
				runBlocking {
					ticket.validate(ticketRepository, ticketTypeRepository, paymentService).executeOnInvalidation()
				}
			}
			.flatMap { ticket ->
				ticketArchiveRepository.insert(ArchivedTicket(
					ticket = ticket,
					invalidatedAt = Instant.now()
				))
					.doOnNext { ticketRepository.deleteById(it.ticket.id!!) }
					.log("ticket.invalidate")
					.flatMap { ok().body(BodyInserters.fromValue(it)) }
			}
			.switchIfEmpty(notFound().build())

	fun downloadTicket(req: ServerRequest) =
		ticketRepository.findById(req.pathVariable("id"))
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
		ticketRepository.findById(req.pathVariable("id"))
			.doOnNext { sendTicketByEmail(it) }
			.flatMap { ok().build() }
			.switchIfEmpty(notFound().build())

	private fun sendTicketByEmail(ticket: Ticket) = GlobalScope.launch {
		ticketService.createPDF(ticket)
			.zipToPairWhen { subscriptionRepository.findById(ticket.subscription.id!!) }
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

	fun Mono<Ticket>.flattenTicket() = flattenTicket(ticketTypeRepository, ticketAttributeRepository)
	fun Ticket.flattenTicket() = flattenTicket(ticketTypeRepository, ticketAttributeRepository)
}

fun Mono<Ticket>.flattenTicket(
	ticketTypeRepository: TicketTypeRepository,
	ticketAttributeRepository: TicketAttributeRepository
) = doOnNext { it.flattenTicket(ticketTypeRepository, ticketAttributeRepository) }

fun Ticket.flattenTicket(
	ticketTypeRepository: TicketTypeRepository,
	ticketAttributeRepository: TicketAttributeRepository
) = ticketTypeRepository.findById(type.id!!).flattenTicketType(ticketAttributeRepository)

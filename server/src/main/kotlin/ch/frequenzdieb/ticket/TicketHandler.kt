package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.DefaultHandlers.asServerResponse
import ch.frequenzdieb.common.ErrorCode
import ch.frequenzdieb.common.RequestParamReader.readQueryParam
import ch.frequenzdieb.common.Validators.Companion.validateAsyncWith
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import ch.frequenzdieb.email.EMailAttachment
import ch.frequenzdieb.email.EmailService
import ch.frequenzdieb.event.EventRepository
import ch.frequenzdieb.payment.PaymentService
import ch.frequenzdieb.payment.datatrans.DatatransPayment
import ch.frequenzdieb.subscription.SubscriptionRepository
import ch.frequenzdieb.ticket.archive.ArchivedTicket
import ch.frequenzdieb.ticket.archive.TicketArchiveRepository
import ch.frequenzdieb.ticket.validation.validate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*
import java.net.URI
import java.time.Instant

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
	private val logger: Logger = LoggerFactory.getLogger(TicketHandler::class.java)

	suspend fun getById(req: ServerRequest): ServerResponse =
		ticketRepository.findById(req.pathVariable("id"))
			.awaitFirstOrNull()
			.let {
				if (it !== null)
					ok().bodyValueAndAwait(it)
				else
					notFound().buildAndAwait()
			}

	suspend fun findAllBySubscriptionIdAndEventId(req: ServerRequest) =
		ticketRepository.findAllBySubscription_IdAndEvent_Id(
			req.readQueryParam("subscriptionId"),
			req.readQueryParam("eventId")
		)
			.asFlow()
			.collect { it.flattenTicket() }
			.asServerResponse()

	// TODO: Check if Tickets still can be bought -> check if event has already started
	// TODO: Check that subscriber in Payload matches User (must not be able to order in the name of another subscriber)
	suspend fun create(req: ServerRequest): ServerResponse =
		req.awaitBody(Ticket::class)
			.validateEntity()
			.apply {
				validate(ticketRepository, ticketTypeRepository, paymentService).executeOnOrder()
			}
			.flattenTicket()
			.let { ticketRepository.insert(it).awaitSingle() }
			.let {
				created(URI.create("${ticketRoute}/${it.id}"))
					.bodyValueAndAwait(TicketQrCode(ticketService.createQRCode(it)))
			}

	suspend fun createPaymentForTicket(req: ServerRequest): ServerResponse =
		req.awaitBody(DatatransPayment::class)
			.validateEntity()
			.validateAsyncWith(ErrorCode.TICKET_ID_INVALID) {
				ticketRepository
					.existsById(req.pathVariable("id"))
					.awaitSingle()
			}
			.let { payment ->
				payment.reference = req.pathVariable("id")
				ok().bodyValueAndAwait(paymentService.initiatePayment(payment))
			}

	suspend fun invalidate(req: ServerRequest): ServerResponse =
		req.awaitBody(TicketQrCode::class)
			.let { ticketQrCode ->
				val ticketId = ticketService.decoder(ticketQrCode.qrCode)
				ticketRepository.findById(ticketId)
					.awaitSingleOrNull()
					.let { ticket ->
						ticket?.apply {
							validate(ticketRepository, ticketTypeRepository, paymentService) {
								onInvalidation { (ticket, type) ->
									eventRepository
										.existsById_AndTicketTypesContains(ticket.event.id, type)
										.awaitSingle() raiseValidationError ErrorCode.TICKET_TYPE_FOR_EVENT_INVALID.toString()
								}
							}.executeOnInvalidation()
						}?.let {
							ticketRepository
								.deleteById(it.id)
								.awaitSingle()

							ticketArchiveRepository
								.insert(ArchivedTicket(
									ticket = it,
									invalidatedAt = Instant.now()
								))
								.awaitSingle()
						}
							?: ticketArchiveRepository.findByTicket_Id(ticketId)
								.awaitSingle()
								.validateWith(ErrorCode.TICKET_ALREADY_USED) { archivedTicket ->
									archivedTicket.invalidatedAt.isBefore(Instant.now())
								}
								.validateWith(ErrorCode.TICKET_ALREADY_ARCHIVED) { false }
					}
					.asServerResponse()
			}

	suspend fun downloadTicket(req: ServerRequest): ServerResponse =
		ticketRepository.findById(req.pathVariable("id"))
			.awaitSingleOrNull()
			.let { ticket ->
				if (ticket !== null) {
					ticketService.createPDF(ticket)
						.let { pdf ->
							ok()
								.contentType(MediaType.APPLICATION_PDF)
								.headers { httpHeaders ->
									httpHeaders.setContentDispositionFormData(
										createTicketName(ticket),
										createTicketName(ticket)
									)
								}
								.bodyValueAndAwait(BodyInserters.fromResource(pdf))
						}
				} else notFound().buildAndAwait()
			}

	suspend fun sendTicket(req: ServerRequest): ServerResponse =
		ticketRepository.findById(req.pathVariable("id"))
			.awaitSingleOrNull()
			?.apply { sendTicketByEmail(this@apply) }
			.asServerResponse(emptyBody = true)

	private suspend fun sendTicketByEmail(ticket: Ticket) =
		ticketService.createPDF(ticket).let { pdf ->
			subscriptionRepository.findById(ticket.subscription.id)
				.awaitSingle()
				.apply {
					emailService.sendEmail(
						emailAddress = email,
						subject = "Dein Ticket zum Frequenzdieb-Konzert",
						message = "Anbei dein Ticket. Wir freuen uns auf einen tollen Abend mit dir!",
						attachment = EMailAttachment(
							attachmentFilename = "ticket_${ticket.id}.pdf",
							file = pdf
						)
					)
				}
		}

	private fun createTicketName(ticket: Ticket) = "ticket_${ticket.id}.pdf"

	suspend fun Ticket.flattenTicket(): Ticket = flattenTicket(ticketTypeRepository, ticketAttributeRepository)
}

suspend fun Ticket.flattenTicket(
	ticketTypeRepository: TicketTypeRepository,
	ticketAttributeRepository: TicketAttributeRepository
): Ticket = apply {
	ticketTypeRepository.findById(type.id)
		.awaitSingle()
		.flattenTicketType(ticketAttributeRepository)
	}

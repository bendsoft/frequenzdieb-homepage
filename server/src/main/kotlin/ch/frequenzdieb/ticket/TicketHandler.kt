package ch.frequenzdieb.ticket

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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import java.net.URI

@Configuration
class TicketHandler(
	private val ticketingRepository: TicketRepository,
	private val subscriptionRepository: SubscriptionRepository,
	private val ticketService: TicketService,
	private val paymentService: PaymentService<DatatransPayment>,
	private val emailService: EmailService
) {
	fun findAllBySubscriptionIdAndEventId(req: ServerRequest) =
		ticketingRepository.findAllBySubscriptionIdAndEventId(
			req.readQueryParam("subscriptionId"),
			req.readQueryParam("eventId")
		).collectList()
			.flatMap { tickets -> ok().bodyValue(tickets) }
			.switchIfEmpty(notFound().build())

	fun create(req: ServerRequest) =
		req.bodyToMono(Ticket::class.java).validateEntity()
			.flatMap { ticketingRepository.insert(it) }
			.flatMap {
				created(URI.create("/ticketing/${it.id}"))
					.bodyValue(TicketCreateResponse(ticketService.createQRCode(it)))
			}

	fun createPaymentForTicket(req: ServerRequest) =
		req.bodyToMono(DatatransPayment::class.java).validateEntity()
			.validateAsyncWith("INVALID_TICKET_ID")
				{ ticketingRepository.existsById(req.pathVariable("id")) }
			.flatMap { datatransPayment ->
				datatransPayment.reference = req.pathVariable("id")
				paymentService.initiatePayment(datatransPayment)
					.flatMap { ok().bodyValue(it) }
			}

	fun invalidate(req: ServerRequest) =
		req.bodyToMono(TicketInvalidationRequest::class.java).validateEntity()
			.zipToPairWhen { ticketingRepository.findById(ticketService.decoder(it.qrCodeValue)) }
			.validateWith ("ALREADY_USED") {
				(_, ticket) -> ticket.isValid
			}
			.validateWith ("ANOTHER_EVENT") {
				(validationRequest, ticket) -> ticket.eventId == validationRequest.eventId
			}
			.validateAsyncWith("NOT_PAYED") {
				(_, ticket) -> paymentService.hasValidPayment(ticket.id!!)
			}
			.flatMap { (_, ticket) ->
				ticket.isValid = false
				ticketingRepository.save(ticket)
					.flatMap {
						println(it)
						ok().body(BodyInserters.fromValue(it))
					}
			}

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
						attachmentFilename = createTicketName(ticket),
						file = pdf
					)
				)
			}.subscribe()
	}

	private fun createTicketName(ticket: Ticket) = "ticket_${ticket.id}.pdf"
}

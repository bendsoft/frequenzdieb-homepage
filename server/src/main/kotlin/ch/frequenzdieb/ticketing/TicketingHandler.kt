package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.RequestParamReader.readQueryParam
import ch.frequenzdieb.common.Validators.Companion.validateAsyncWith
import ch.frequenzdieb.common.Validators.Companion.validateEntity
import ch.frequenzdieb.common.Validators.Companion.validateWith
import ch.frequenzdieb.email.EMailAttachment
import ch.frequenzdieb.email.EmailService
import ch.frequenzdieb.payment.PaymentService
import ch.frequenzdieb.payment.datatrans.DatatransPayment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class TicketingHandler(
	private val ticketingRepository: TicketRepository,
	private val ticketService: TicketService,
	private val paymentService: PaymentService<DatatransPayment>,
	private val emailService: EmailService
) {
	fun findAllBySubscriptionIdAndEventId(req: ServerRequest) =
		ticketingRepository.findAllBySubscription_IdAndEvent_Id(
			req.readQueryParam("subscriptionid"),
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
			.zipWhen { ticketingRepository.findById(ticketService.decoder(it.qrCodeValue)) }
			.validateWith ("ALREADY_USED") { !it.t2.isValid }
			.validateWith ("ANOTHER_EVENT") { it.t2.event.id != it.t1.eventId }
			.validateAsyncWith("NOT_PAYED") { paymentService.hasValidPayment(it.t2.id!!) }
			.flatMap { ticket ->
				ticket.t2.isValid = false
				ticketingRepository.save(ticket.t2)
					.flatMap { ok().bodyValue(it) }
			}
			.switchIfEmpty(notFound().build())

	fun downloadTicket(req: ServerRequest) =
		ticketingRepository.findById(req.pathVariable("id"))
			.zipWhen { ticketService.createPDF(it) }
			.flatMap {
				ok()
					.contentType(MediaType.APPLICATION_PDF)
					.headers { httpHeaders ->
						httpHeaders.setContentDispositionFormData(createTicketName(it.t1), createTicketName(it.t1))
					}
					.body(BodyInserters.fromResource(it.t2))
			}
			.switchIfEmpty(notFound().build())

	fun sendTicket(req: ServerRequest) =
		ticketingRepository.findById(req.pathVariable("id"))
			.doOnNext { sendTicketByEmail(it) }
			.flatMap { ok().build() }
			.switchIfEmpty(notFound().build())

	private fun sendTicketByEmail(ticket: Ticket) = GlobalScope.launch {
		ticketService.createPDF(ticket)
			.doOnSuccess {
				emailService.sendEmail(
					emailAddress = ticket.subscription.email,
					subject = "Dein Ticket zum Frequenzdieb-Konzert",
					message = "Anbei dein Ticket. Wir freuen uns auf einen tollen Abend mit dir!",
					attachment = EMailAttachment(
						attachmentFilename = createTicketName(ticket),
						file = it
					)
				)
			}.subscribe()
	}

	private fun createTicketName(ticket: Ticket) = "ticket_${ticket.id}.pdf"
}

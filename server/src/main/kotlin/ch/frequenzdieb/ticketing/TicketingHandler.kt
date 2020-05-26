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
	fun findAllBySubscriptionId(req: ServerRequest) =
		req.readQueryParam("subscriptionid")
			.flatMap {
				ticketingRepository.findAllBySubscriptionId(it).collectList()
					.flatMap { tickets -> ok().bodyValue(tickets) }
					.switchIfEmpty(notFound().build())
			}

	fun create(req: ServerRequest) =
		req.bodyToMono(Ticket::class.java).validateEntity()
			.flatMap { ticketingRepository.insert(it) }
			.flatMap {
				created(URI.create("/ticketing/${it.id}"))
					.bodyValue(TicketCreateResponse(ticketService.createQRCode(it)))
			}

	fun createPaymentForTicket(req: ServerRequest) =
		Mono.justOrEmpty(req.pathVariable("id"))
			.validateAsyncWith("INVALID_TICKET_ID")
				{ ticketingRepository.existsById(req.pathVariable("id")) }
			.flatMap { ticketId ->
				req.bodyToMono(DatatransPayment::class.java).validateEntity()
					.flatMap { datatransPayment ->
						datatransPayment.reference = ticketId
						paymentService.initiatePayment(datatransPayment)
							.flatMap { ok().bodyValue(it) }
					}
			}

	fun invalidate(req: ServerRequest) =
		req.bodyToMono(TicketInvalidationRequest::class.java).validateEntity()
			.flatMap { invalidationRequest ->
				val ticketId = ticketService.decoder(invalidationRequest.qrCodeValue)
				paymentService.hasValidPayment(ticketId)
					.flatMap { hasValidPayment ->
						if (hasValidPayment == false) {
							badRequest().bodyValue("NOT_PAYED")
						} else {
							ticketingRepository.findById(ticketId)
								.validateWith ("ALREADY_USED") { !it.isValid }
								.validateWith ("ANOTHER_EVENT") { invalidationRequest.eventId != it.event.id }
								.flatMap { ticket ->
									ticket.isValid = false
									ticketingRepository.save(ticket).flatMap {
										ok().bodyValue(it)
									}
								}
								.switchIfEmpty(notFound().build())
						}
					}
			}

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

package ch.frequenzdieb.api.services.ticketing

import ch.frequenzdieb.api.services.common.EMailAttachment
import ch.frequenzdieb.api.services.common.EmailService
import ch.frequenzdieb.api.services.subscription.SubscriptionRepository
import ch.frequenzdieb.api.services.ticketing.payment.TransactionRepository
import ch.frequenzdieb.api.services.ticketing.payment.datatrans.DatatransUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
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
import java.time.LocalDateTime

@Configuration
class TicketingHandler {
	@Autowired
	lateinit var ticketingRepository: TicketingRepository

	@Autowired
	lateinit var ticketUtils: TicketUtils

	@Autowired
	lateinit var transactionRepository: TransactionRepository

	@Autowired
	lateinit var datatransUtils: DatatransUtils

	@Autowired
	lateinit var subscriptionRepository: SubscriptionRepository

	@Autowired
	lateinit var emailService: EmailService

	fun findAllBySubscriptionId(req: ServerRequest) =
		Mono.justOrEmpty(req.queryParam("subscriptionid"))
			.filterWhen { subscriptionRepository.findById(it).hasElement() }
			.flatMap {
				ticketingRepository.findAllBySubscriptionId(it).collectList()
					.flatMap { tickets -> ok().bodyValue(tickets) }
					.switchIfEmpty(notFound().build())
			}
			.switchIfEmpty(badRequest().bodyValue("Please provide a valid subscriptionId"))

	fun create(req: ServerRequest) =
		req.bodyToMono(Ticket::class.java)
			.flatMap {
				it.created = LocalDateTime.now()
				ticketingRepository.insert(it)
			}
			.flatMap {
				created(URI.create("/ticketing/${it.id}"))
					.bodyValue(hashMapOf(
						"qrcode" to ticketUtils.createQRCode(it)
					))
			}
			.switchIfEmpty(badRequest().bodyValue("Ticket entity must be provided"))

	fun createPaymentTransactionReference(req: ServerRequest) =
		ticketingRepository.findById(req.pathVariable("id"))
			.map { datatransUtils.createPaymentTransactionRefHash(it) }
			.flatMap { ok().bodyValue(it) }
			.switchIfEmpty(badRequest().bodyValue("Ticket is not valid"))

	fun invalidate(req: ServerRequest) =
		req.bodyToMono(TicketInvalidationRequest::class.java)
			.flatMap { invalidationRequestTicket ->
				ticketingRepository.findOneByQrCodeHash(invalidationRequestTicket.qrCodeHash)
					.filter { it.isValid && checkTicketIntegrity(it) }
					.filterWhen { hasValidPaymentTransaction(it) }
					.doOnNext {
						it.isValid = false
						ticketingRepository.save(it)
					}
					.flatMap { ok().body(it, Ticket::class.java) }
					.switchIfEmpty(badRequest().bodyValue("Ticket is not valid"))
			}

	private data class TicketInvalidationRequest(
		var qrCodeHash: String
	)

	private fun checkTicketIntegrity(ticket: Ticket) =
		ticketUtils.createUniqueTicketHash(ticket) == ticket.qrCodeHash

	fun downloadTicket(req: ServerRequest) =
		ticketingRepository.findById(req.pathVariable("id"))
			.flatMap {
				ok()
					.contentType(MediaType.APPLICATION_PDF)
					.headers { httpHeaders ->
						httpHeaders.setContentDispositionFormData(createTicketName(it), createTicketName(it))
					}
					.body(BodyInserters.fromResource(ticketUtils.createPDF(it)))
			}
			.switchIfEmpty(notFound().build())

	fun sendTicket(req: ServerRequest) =
		ticketingRepository.findById(req.pathVariable("id"))
			.doOnNext { sendTicketByEmail(it) }
			.flatMap { ok().build() }
			.switchIfEmpty(notFound().build())

	private fun sendTicketByEmail(ticket: Ticket) = GlobalScope.launch {
		subscriptionRepository.findById(ticket.subscriptionId)
			.doOnSuccess {
				emailService.sendEmail(
					emailAddress = it.email,
					subject = "Dein Ticket zum Frequenzdieb-Konzert",
					message = "Anbei dein Ticket. Wir freuen uns auf einen tollen Abend mit dir!",
					attachment = EMailAttachment(
						attachmentFilename = createTicketName(ticket),
						file = ticketUtils.createPDF(ticket)
					)
				)
			}.subscribe()
	}

	private fun createTicketName(ticket: Ticket) = "ticket_${ticket.id}.pdf"

	private fun hasValidPaymentTransaction(ticket: Ticket) =
		Mono.justOrEmpty(datatransUtils.createPaymentTransactionRefHash(ticket))
			.flatMap { transactionRepository.findTopByRefnoAndSuccess_ResponseCode(it) }
			.filter { datatransUtils.isTransactionSuccessful(it) }
			.hasElement()
}

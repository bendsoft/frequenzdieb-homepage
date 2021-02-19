package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.event.concert.ConcertHelper
import ch.frequenzdieb.subscription.Subscription
import ch.frequenzdieb.subscription.SubscriptionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class TicketingHelper {
    @Autowired lateinit var concertHelper: ConcertHelper
    @Autowired lateinit var subscriptionHelper: SubscriptionHelper
    @Autowired lateinit var ticketTypeHelper: TicketTypeHelper
    @Autowired lateinit var ticketAttributeHelper: TicketAttributeHelper

    suspend fun createFakeTicket(
        subscription: Subscription? = null,
        event: Event? = null,
        type: TicketType? = null
    ) = Ticket(
        subscriptionId = createSubscriptionId(),
        eventId = createEventId(event),
        type = createTicketType(type)
    )

    private suspend fun createTicketType(type: TicketType?) =
        type ?: ticketTypeHelper.createTicketType().insert()

    private suspend fun createEventId(event: Event?) =
        (event ?: concertHelper.createConcert().insert()).id!!

    private suspend fun createSubscriptionId() =
        (subscriptionHelper.createSubscriptionForHans(createRandomString(5)).insert()).id!!

    suspend fun createFakeTickets(
        amount: Int,
        ticketProducer: suspend TicketingHelper.() -> Ticket = { createFakeTicket() }
    ): List<Ticket> =
        (1..amount).map {
            ticketProducer()
        }
}

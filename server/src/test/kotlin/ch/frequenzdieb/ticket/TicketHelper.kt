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
import java.util.*

@Component
@AutoConfigureDataMongo
internal class TicketHelper {
    @Autowired lateinit var subscriptionHelper: SubscriptionHelper
    @Autowired lateinit var ticketTypeHelper: TicketTypeHelper
    @Autowired lateinit var ticketAttributeHelper: TicketAttributeHelper
    @Autowired lateinit var concertHelper: ConcertHelper

    suspend fun createFakeTicket(
        subscription: Subscription? = null,
        event: Event? = null,
        type: TicketType? = null
    ): Ticket {
        val fakeTicketType = type ?: ticketTypeHelper.createTicketType()

        return Ticket(
            subscription = subscriptionHelper.createSubscriptionForHans(createRandomString(5)).insert(),
            type = fakeTicketType,
            event = event ?: concertHelper.createConcert().insert(),
        )
    }

    suspend fun createFakeTickets(
        amount: Int,
        ticketProducer: suspend TicketHelper.() -> Ticket = { createFakeTicket() }
    ): List<Ticket> =
        (1..amount).map {
            ticketProducer()
        }

    fun createNewFakeTicketFrom(fakeTickets: List<Ticket>) = fakeTickets
        .first()
        .copy()
        .apply {
            id = UUID.randomUUID().toString()
        }
}

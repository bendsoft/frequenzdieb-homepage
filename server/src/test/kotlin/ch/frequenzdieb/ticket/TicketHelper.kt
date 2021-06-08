package ch.frequenzdieb.ticket

import ch.frequenzdieb.common.BaseHelper.Dsl.createRandomString
import ch.frequenzdieb.common.BaseHelper.Dsl.insert
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.event.concert.ConcertHelper
import ch.frequenzdieb.subscription.Subscription
import ch.frequenzdieb.subscription.SubscriptionHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.stereotype.Component
import java.util.*

@Component
@AutoConfigureDataMongo
internal class TicketHelper {
    @Autowired lateinit var subscriptionHelper: SubscriptionHelper
    @Autowired lateinit var ticketTypeHelper: TicketTypeHelper
    @Autowired lateinit var concertHelper: ConcertHelper

    suspend fun createFakeTicket(
        subscription: Subscription? = null,
        event: Event? = null,
        type: TicketType? = null
    ): Ticket {
        val fakeTicketType = type ?: ticketTypeHelper.createTicketType().insert()
        val fakeSubscription = subscription ?: subscriptionHelper.createSubscriptionForHans(createRandomString(5)).insert()
        val fakeEvent = event ?: concertHelper.createConcert().insert()

        return Ticket(
            subscription = fakeSubscription,
            type = fakeTicketType,
            event = fakeEvent,
        )
    }

    suspend fun createFakeTickets(
        amount: Int,
        ticketProducer: suspend TicketHelper.() -> Ticket = { createFakeTicket() }
    ): Flow<Ticket> = flow {
        repeat(amount) {
            emit(ticketProducer())
        }
    }

    fun createNewFakeTicketFrom(fakeTicket: Ticket) = fakeTicket
        .copy()
        .apply {
            id = UUID.randomUUID().toString()
        }

    fun createNewFakeTicketFrom(fakeTickets: List<Ticket>) = fakeTickets
        .first().let { createNewFakeTicketFrom(it) }
}

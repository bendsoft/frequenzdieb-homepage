package ch.frequenzdieb.ticketing

import ch.frequenzdieb.common.BaseHelper
import ch.frequenzdieb.event.Event
import ch.frequenzdieb.event.concert.ConcertHelper
import ch.frequenzdieb.subscription.Subscription
import ch.frequenzdieb.subscription.SubscriptionHelper
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@AutoConfigureDataMongo
internal class TicketingHelper(
    mongoTemplate: MongoTemplate,
    private val concertHelper: ConcertHelper,
    private val subscriptionHelper: SubscriptionHelper,
    private val ticketTypeHelper: TicketTypeHelper
) : BaseHelper(mongoTemplate, Ticket::class.java) {
    fun createFakeStandingTicket(
        subscription: Subscription = subscriptionHelper.createSubscriptionForHans(createRandomString(5)).insert(),
        event: Event = concertHelper.createConcert().insert(),
        type: TicketType = ticketTypeHelper.createTicketType().insert()
    ) =
        Ticket(
            subscription = subscription,
            event = event,
            type = type,
            isValid = true
        )

    fun createFakeStandingTicket(
        amount: Int,
        ticketProducer: TicketingHelper.() -> Ticket = { createFakeStandingTicket() }
    ): List<Ticket> =
        (1..amount).map {
            ticketProducer()
        }
}

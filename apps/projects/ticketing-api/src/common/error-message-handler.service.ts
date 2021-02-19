import { HttpErrorResponse } from '@angular/common/http'
import { concat, toPairs } from 'lodash-es'
import { Inject, Injectable, InjectionToken, Optional } from '@angular/core'

export interface DefaultErrorMessages {
  INVALID_REQUEST: string
  UNKNOWN_ERROR: string
  SERVER_ERROR: string
  NOT_FOUND: string
  NOT_AUTHORIZED: string
  SIGNATURE_INVALID: string
  VALIDATION_ERROR: string
  ENTITY_INVALID: string
  EMAIL_INVALID: string
}

export interface TicketErrorMessages {
  TICKET_MISSING_SUBSCRIPTION: string
  TICKET_ID_INVALID: string
  TICKET_ALREADY_USED: string
  TICKET_FOR_ANOTHER_EVENT: string
  TICKET_NOT_PAID: string
  TICKET_TYPE_DUPLICATE_TEMPLATE_TAG: string
  TICKET_TYPE_DUPLICATE_ATTRIBUTE_KEY: string
}

export interface SubscriptionErrorMessages {
  SUBSCRIPTION_INVALID_ID: string
  SUBSCRIPTION_NOT_EXISTS: string
  SUBSCRIPTION_ALREADY_CONFIRMED: string
}

export const DEFAULT_ERROR_MESSAGES = new InjectionToken<DefaultErrorMessages>(
  'DefaultErrorMessages'
)

export const TICKET_ERROR_MESSAGES = new InjectionToken<TicketErrorMessages>('TicketErrorMessages')

export const SUBSCRIPTION_ERROR_MESSAGES = new InjectionToken<SubscriptionErrorMessages>(
  'SubscriptionErrorMessages'
)

@Injectable({
  providedIn: 'root'
})
export class ErrorMessageHandler {
  private errorMessages = new Map<string, string>(
    toPairs({
      INVALID_REQUEST: 'Something went wrong with the request',
      UNKNOWN_ERROR: 'An unknown error has happened',
      SERVER_ERROR: 'An error happened on the server-side',
      NOT_FOUND: 'Not found',
      NOT_AUTHORIZED: 'Not authorized or access-token missing or invalid',
      SIGNATURE_INVALID: 'The signature is invalid',
      VALIDATION_ERROR: 'A validation error has occurred',
      ENTITY_INVALID: 'Entity is invalid',
      EMAIL_INVALID: 'E-mail is invalid',
      TICKET_MISSING_SUBSCRIPTION: 'The subscription for the ticket could not be found',
      TICKET_ID_INVALID: 'Ticket-ID is invalid',
      TICKET_ALREADY_USED: 'Ticket has already been used',
      TICKET_FOR_ANOTHER_EVENT: 'Ticket is for another event',
      TICKET_NOT_PAID: 'No payment found for given ticket',
      TICKET_TYPE_DUPLICATE_TEMPLATE_TAG: 'The same tag cannot be used twice',
      TICKET_TYPE_DUPLICATE_ATTRIBUTE_KEY:
        'Multiple attributes with the same key cannot be used twice for a ticket-type',
      SUBSCRIPTION_INVALID_ID: 'Subscription is invalid',
      SUBSCRIPTION_NOT_EXISTS: 'Subscription not found',
      SUBSCRIPTION_ALREADY_CONFIRMED: 'Subscription is already confirmed'
    })
  )

  constructor(
    @Optional() @Inject(DEFAULT_ERROR_MESSAGES) private defaultErrorMessages,
    @Optional() @Inject(TICKET_ERROR_MESSAGES) private ticketErrorMessages,
    @Optional() @Inject(SUBSCRIPTION_ERROR_MESSAGES) private subscriptionErrorMessages
  ) {
    concat(
      toPairs(defaultErrorMessages),
      toPairs(ticketErrorMessages),
      toPairs(subscriptionErrorMessages)
    ).forEach(([key, value]) => {
      this.errorMessages.set(key, value)
    })
  }

  private static createHttpErrorResponseWithMessage(
    originalResponse: HttpErrorResponse,
    error: string
  ) {
    return new HttpErrorResponse({
      error,
      headers: originalResponse.headers,
      status: originalResponse.status,
      statusText: originalResponse.statusText,
      url: originalResponse.url
    })
  }

  getErrorMessageFromResponse(response: HttpErrorResponse) {
    try {
      const serializedErrorMessage = JSON.parse(response.error.message)
      if (this.errorMessages.has(serializedErrorMessage.code)) {
        return ErrorMessageHandler.createHttpErrorResponseWithMessage(
          response,
          this.errorMessages.get(serializedErrorMessage.code)
        )
      }
    } catch (e) {
      console.log(e)
    }

    switch (response.status) {
      case 404:
        return ErrorMessageHandler.createHttpErrorResponseWithMessage(
          response,
          this.errorMessages.get('NOT_FOUND')
        )
      case 401:
        return ErrorMessageHandler.createHttpErrorResponseWithMessage(
          response,
          this.errorMessages.get('NOT_AUTHORIZED')
        )
      case 403:
        return ErrorMessageHandler.createHttpErrorResponseWithMessage(
          response,
          this.errorMessages.get('INVALID_REQUEST')
        )
      default: {
        if (ErrorMessageHandler.hasServerError(response)) {
          return ErrorMessageHandler.createHttpErrorResponseWithMessage(
            response,
            this.errorMessages.get('SERVER_ERROR')
          )
        }
        return ErrorMessageHandler.createHttpErrorResponseWithMessage(
          response,
          this.errorMessages.get('UNKNOWN_ERROR')
        )
      }
    }
  }

  static hasServerError(response: HttpErrorResponse) {
    return response.status / 100 >= 5
  }
}

import { HttpErrorResponse } from '@angular/common/http'
import { concat, toPairs } from 'lodash'
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
  TICKET_DUPLICATE_TEMPLATE_TAG: string
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
      INVALID_REQUEST: 'Something went wrong with the Request.',
      UNKNOWN_ERROR: 'An unknown Error has happened.',
      SERVER_ERROR: 'An Error happened on the server-side.',
      NOT_FOUND: 'Not found.',
      NOT_AUTHORIZED: 'Not Authorized or Access-Token missing or invalid.',
      SIGNATURE_INVALID: '',
      VALIDATION_ERROR: '',
      ENTITY_INVALID: '',
      EMAIL_INVALID: '',
      TICKET_MISSING_SUBSCRIPTION: '',
      TICKET_ID_INVALID: '',
      TICKET_ALREADY_USED: 'Ticket has already been used',
      TICKET_FOR_ANOTHER_EVENT: '',
      TICKET_NOT_PAID: 'No payment found for given ticket',
      TICKET_DUPLICATE_TEMPLATE_TAG: '',
      SUBSCRIPTION_INVALID_ID: '',
      SUBSCRIPTION_NOT_EXISTS: '',
      SUBSCRIPTION_ALREADY_CONFIRMED: ''
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
    const serializedErrorMessage = JSON.parse(response.error.message)
    if (this.errorMessages.has(serializedErrorMessage.code)) {
      return ErrorMessageHandler.createHttpErrorResponseWithMessage(
        response,
        this.errorMessages.get(serializedErrorMessage.code)
      )
    }

    switch (response.status) {
      case 404:
        return ErrorMessageHandler.createHttpErrorResponseWithMessage(
          response,
          this.defaultErrorMessages.NOT_FOUND
        )
      case 401:
        return ErrorMessageHandler.createHttpErrorResponseWithMessage(
          response,
          this.defaultErrorMessages.NOT_AUTHORIZED
        )
      case 403:
        return ErrorMessageHandler.createHttpErrorResponseWithMessage(
          response,
          this.defaultErrorMessages.INVALID_REQUEST
        )
      default: {
        if (ErrorMessageHandler.hasServerError(response)) {
          return ErrorMessageHandler.createHttpErrorResponseWithMessage(
            response,
            this.defaultErrorMessages.SERVER_ERROR
          )
        }
        return ErrorMessageHandler.createHttpErrorResponseWithMessage(
          response,
          this.defaultErrorMessages.UNKNOWN_ERROR
        )
      }
    }
  }

  static hasServerError(response: HttpErrorResponse) {
    return response.status / 100 >= 5
  }
}

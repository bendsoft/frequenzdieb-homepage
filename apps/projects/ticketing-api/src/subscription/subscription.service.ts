import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { ApiContextService } from '../api-context.service'
import { Subscription } from '../@types/subscription'
import { catchServerError } from '../common/error-message-handler.service'

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly subscriptionRoute: string
  private readonly serverErrorCatcher

  constructor(private httpClient: HttpClient, private apiContext: ApiContextService) {
    this.serverErrorCatcher = catchServerError()
    this.subscriptionRoute = `${apiContext.apiServerUrl}/subscription`
  }

  get(subscriptionId: string) {
    return this.httpClient
      .get<Subscription>(
        `${this.subscriptionRoute}/${subscriptionId}`,
        this.apiContext.createWithAuthorizationHeaders()
      )
      .pipe(this.serverErrorCatcher)
  }

  getByEmail(email: string) {
    return this.apiContext.enrichApiRequestWithRecaptcha(
      '',
      (httpOptions) =>
        this.httpClient
          .get<Subscription>(`${this.subscriptionRoute}`, httpOptions)
          .pipe(this.serverErrorCatcher),
      {
        params: { email }
      }
    )
  }

  update(updatedSubscription: Subscription) {
    return this.apiContext.enrichApiRequestWithRecaptcha('requestSubscriptionDeletion', () =>
      this.httpClient.put(this.subscriptionRoute, updatedSubscription).pipe(this.serverErrorCatcher)
    )
  }

  requestEmailConfirmation(id: string) {
    return this.apiContext.enrichApiRequestWithRecaptcha(
      'requestEmailConfirmation',
      (httpOptions) =>
        this.httpClient.get(`${this.subscriptionRoute}/${id}/resend-confirmation`, httpOptions)
    )
  }

  confirmEmail(id: string, signature: string) {
    return this.httpClient
      .get(`${this.subscriptionRoute}/${id}/confirm`, {
        params: { signature }
      })
      .pipe(this.serverErrorCatcher)
  }

  create(newSubscription: Subscription) {
    return this.apiContext.enrichApiRequestWithRecaptcha('createSubscription', (httpOptions) =>
      this.httpClient
        .post(`${this.subscriptionRoute}`, newSubscription, httpOptions)
        .pipe(this.serverErrorCatcher)
    )
  }

  delete(id: string, signature: string) {
    return this.httpClient
      .delete(`${this.subscriptionRoute}/${id}`, {
        params: { signature }
      })
      .pipe(this.serverErrorCatcher)
  }

  sendDeletionConfirmationEmail(email: string) {
    return this.apiContext.enrichApiRequestWithRecaptcha(
      'sendDeletionConfirmationEmail',
      (httpOptions) =>
        this.httpClient
          .get(`${this.subscriptionRoute}/remove`, httpOptions)
          .pipe(this.serverErrorCatcher),
      {
        params: { email }
      }
    )
  }
}

import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { ApiContextService, catchServerError } from '../api-context.service'
import { Subscription } from '../@types/subscription'

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private get subscriptionRoute() {
    return `${this.apiContext.getApiServer()}/subscription`
  }
  private readonly serverErrorCatcher

  constructor(private httpClient: HttpClient, private apiContext: ApiContextService) {
    this.serverErrorCatcher = catchServerError()
  }

  get(subscriptionId: string): Observable<Subscription> {
    return this.httpClient
      .get<Subscription>(
        `${this.subscriptionRoute}/${subscriptionId}`,
        this.apiContext.createWithAuthorizationHeaders()
      )
      .pipe(this.serverErrorCatcher)
  }

  getByEmail(email: string): Observable<Subscription> {
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

  update(updatedSubscription: Subscription): Observable<Subscription> {
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

  create(newSubscription: Subscription): Observable<Subscription> {
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

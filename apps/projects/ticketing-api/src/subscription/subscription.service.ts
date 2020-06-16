import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { ApiContextService } from '../api-context.service'
import { Subscription } from '../@types/subscription'

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly subscriptionRoute: string

  constructor(
    private httpClient: HttpClient,
    private apiContext: ApiContextService
  ) {
    this.subscriptionRoute = `${apiContext.apiServerUrl}/subscription`
  }

  get(subscriptionId: string): Observable<Subscription> {
    return this.httpClient
      .get<Subscription>(
        `${this.subscriptionRoute}/${subscriptionId}`,
        this.apiContext.createWithAuthorizationHeaders()
      )
      .pipe(this.apiContext.translateServerError())
  }

  getByEmail(email: string): Observable<Subscription> {
    return this.apiContext.enrichApiRequestWithRecaptcha(
      '',
      (httpOptions) =>
        this.httpClient
          .get<Subscription>(`${this.subscriptionRoute}`, httpOptions)
          .pipe(this.apiContext.translateServerError()),
      {
        params: { email }
      }
    )
  }

  update(updatedSubscription: Subscription) {
    return this.apiContext.enrichApiRequestWithRecaptcha(
      'requestSubscriptionDeletion',
      () => this.httpClient.put(this.subscriptionRoute, updatedSubscription)
    )
  }

  requestEmailConfirmation(id: string) {
    return this.apiContext.enrichApiRequestWithRecaptcha(
      'requestEmailConfirmation',
      (httpOptions) =>
        this.httpClient.get(
          `${this.subscriptionRoute}/${id}/resend-confirmation`,
          httpOptions
        )
    )
  }

  confirmEmail(id: string, signature: string) {
    return this.httpClient.get(`${this.subscriptionRoute}/${id}/confirm`, {
      params: { signature }
    })
  }

  create(newSubscription: Subscription) {
    return this.apiContext.enrichApiRequestWithRecaptcha(
      'createSubscription',
      (httpOptions) =>
        this.httpClient.post(
          `${this.subscriptionRoute}`,
          newSubscription,
          httpOptions
        )
    )
  }

  delete(id: string, signature: string) {
    return this.httpClient.delete(`${this.subscriptionRoute}/${id}`, {
      params: { signature }
    })
  }

  sendDeletionConfirmationEmail(email: string) {
    return this.apiContext.enrichApiRequestWithRecaptcha(
      'sendDeletionConfirmationEmail',
      (httpOptions) =>
        this.httpClient.get(`${this.subscriptionRoute}/remove`, httpOptions),
      {
        params: { email }
      }
    )
  }
}

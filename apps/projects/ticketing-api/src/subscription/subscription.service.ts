import { Injectable } from '@angular/core'
import { HttpClient, HttpErrorResponse } from '@angular/common/http'
import { ReCaptchaV3Service } from 'ng-recaptcha'
import { merge } from 'lodash'

import { Observable, throwError } from 'rxjs'
import { catchError, switchMap } from 'rxjs/operators'
import { Subscription } from './Subscription'
import { ApiContextService } from '../api-context.service'
import { LocalizedErrorMessage } from '../common/LocalizedErrorMessage'

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly subscriptionRoute: string

  constructor(
    private httpClient: HttpClient,
    private apiContext: ApiContextService,
    private localizedErrorMessage: LocalizedErrorMessage,
    private recaptcha: ReCaptchaV3Service
  ) {
    this.subscriptionRoute = `${apiContext.apiServerUrl}/subscription`
  }

  get(subscriptionId: string): Observable<Subscription> {
    return this.httpClient
      .get<Subscription>(
        `${this.subscriptionRoute}/${subscriptionId}`,
        this.apiContext.createWithAuthorizationHeaders()
      )
      .pipe(
        catchError((response: HttpErrorResponse) =>
          throwError(
            this.localizedErrorMessage.getErrorMessageFromResponse(response)
          )
        )
      )
  }

  update(updatedSubscription: Subscription) {
    return this.enrichApiRequestWithRecaptcha(
      'requestSubscriptionDeletion',
      () => this.httpClient.put(this.subscriptionRoute, updatedSubscription)
    )
  }

  public enrichApiRequestWithRecaptcha<
    T extends Partial<{
      params: Record<string, string | string[]>
    }>,
    R
  >(
    actionName: string,
    httpRequestCallback: (
      httpOptions: T & { params: { recaptcha: string } }
    ) => Observable<R>,
    options?: T
  ): Observable<R> {
    return this.recaptcha.execute(actionName).pipe(
      switchMap((token) =>
        httpRequestCallback(
          merge({}, options, {
            params: { recaptcha: token }
          })
        )
      )
    )
  }

  requestEmailConfirmation(id) {
    return this.enrichApiRequestWithRecaptcha(
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

  create(
    email: string,
    name: string,
    surname: string,
    isNewsletterAccepted: boolean
  ) {
    return this.enrichApiRequestWithRecaptcha(
      'createSubscription',
      (httpOptions) =>
        this.httpClient.post(
          `${this.subscriptionRoute}`,
          {
            name,
            surname,
            email,
            isNewsletterAccepted
          },
          httpOptions
        )
    )
  }

  delete(id, signature) {
    return this.httpClient.delete(`${this.subscriptionRoute}/${id}`, {
      params: { signature }
    })
  }

  sendDeletionConfirmationEmail(email) {
    return this.enrichApiRequestWithRecaptcha(
      'sendDeletionConfirmationEmail',
      (httpOptions) =>
        this.httpClient.get(`${this.subscriptionRoute}/remove`, httpOptions),
      {
        params: { email }
      }
    )
  }
}

import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { ReCaptchaV3Service } from 'ng-recaptcha'
import { merge } from 'lodash'
import { Observable } from 'rxjs'
import { switchMap } from 'rxjs/operators'

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  api_url = 'https://dev-api.frequenzdieb.ch/subscription'

  constructor(
    private http: HttpClient,
    private recaptcha: ReCaptchaV3Service
  ) {}

  getSubscription(email) {
    return this.enrichApiRequestWithRecaptcha(
      'getSubscription',
      (httpOptions) => this.http.get(this.api_url, httpOptions),
      {
        params: { email }
      }
    )
  }

  requestEmailConfirmation(id) {
    return this.enrichApiRequestWithRecaptcha(
      'requestEmailConfirmation',
      (httpOptions) =>
        this.http.get(`${this.api_url}/${id}/resend-confirmation`, httpOptions)
    )
  }

  confirmEmail(id: string, signature: string) {
    return this.http.get(`${this.api_url}/${id}/confirm`, {
      params: { signature }
    })
  }

  createSubscription(
    email: string,
    name: string,
    surname: string,
    isNewsletterAccepted: boolean
  ) {
    return this.enrichApiRequestWithRecaptcha(
      'createSubscription',
      (httpOptions) =>
        this.http.post(
          `${this.api_url}`,
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

  deleteSubscription(id, signature) {
    return this.http.delete(`${this.api_url}/${id}`, {
      params: { signature }
    })
  }

  requestSubscriptionDeletion(email) {
    return this.enrichApiRequestWithRecaptcha(
      'requestSubscriptionDeletion',
      (httpOptions) => this.http.get(`${this.api_url}/remove`, httpOptions),
      {
        params: { email }
      }
    )
  }

  updateSubscription(id, updatedSubscription) {
    return this.enrichApiRequestWithRecaptcha(
      'requestSubscriptionDeletion',
      () => this.http.put(`${this.api_url}/${id}`, updatedSubscription)
    )
  }

  private enrichApiRequestWithRecaptcha<
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
}

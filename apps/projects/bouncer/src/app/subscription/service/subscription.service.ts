import { Injectable } from '@angular/core'
import { HttpClient, HttpErrorResponse } from '@angular/common/http'
import { Observable, throwError } from 'rxjs'
import { catchError } from 'rxjs/operators'
import { ApplicationContextService } from '../../common/service/application-context.service'
import { Subscription } from '../Subscription'
import { LocalizedErrorMessage } from '../../common/LocalizedErrorMessage'

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly subscriptionRoute

  constructor(
    private httpClient: HttpClient,
    private applicationContext: ApplicationContextService
  ) {
    this.subscriptionRoute = `${applicationContext.apiServerUrl}/subscription`
  }

  get(subscriptionId: string): Observable<Subscription> {
    return this.httpClient
      .get<Subscription>(
        `${this.subscriptionRoute}/${subscriptionId}`,
        this.applicationContext.createWithAuthorizationHeaders()
      )
      .pipe(
        catchError((response: HttpErrorResponse) =>
          throwError(
            LocalizedErrorMessage.getErrorMessageFromResponse(response)
          )
        )
      )
  }
}

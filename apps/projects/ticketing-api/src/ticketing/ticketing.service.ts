import { Injectable } from '@angular/core'
import { HttpClient, HttpErrorResponse } from '@angular/common/http'
import { Observable, throwError } from 'rxjs'
import { catchError } from 'rxjs/operators'
import { Ticket } from './Ticket'
import { ApiContextService } from '../api-context.service'
import { LocalizedErrorMessage } from '../common/LocalizedErrorMessage'

@Injectable({
  providedIn: 'root'
})
export class TicketingService {
  private readonly invalidateTicketApiUrl

  constructor(
    private httpClient: HttpClient,
    private context: ApiContextService,
    private localizedErrorMessage: LocalizedErrorMessage
  ) {
    this.invalidateTicketApiUrl = `${context.apiServerUrl}/ticketing/invalidate`
  }

  invalidate(qrCodeValue: string, eventId: string): Observable<Ticket> {
    return this.httpClient
      .put<Ticket>(
        this.invalidateTicketApiUrl,
        { qrCodeValue, eventId },
        this.context.createWithAuthorizationHeaders()
      )
      .pipe(
        catchError((response: HttpErrorResponse) =>
          throwError(
            this.localizedErrorMessage.getErrorMessageFromResponse(response)
          )
        )
      )
  }
}

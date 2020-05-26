import { Injectable } from '@angular/core'
import { HttpClient, HttpErrorResponse } from '@angular/common/http'
import { Observable, of, throwError } from 'rxjs'
import { catchError } from 'rxjs/operators'
import { ApplicationContextService } from '../../common/service/application-context.service'
import { Ticket } from '../Ticket'
import { environment } from '../../../environments/environment'
import { TicketInvalidationError } from './TicketInvalidationError'
import { LocalizedErrorMessage } from '../../common/LocalizedErrorMessage'

@Injectable({
  providedIn: 'root'
})
export class TicketingService {
  private readonly invalidateTicketApiUrl

  constructor(
    private httpClient: HttpClient,
    private applicationContext: ApplicationContextService
  ) {
    this.invalidateTicketApiUrl = `${applicationContext.apiServerUrl}/ticketing/invalidate`
  }

  invalidate(qrCodeValue: string, eventId: string): Observable<Ticket> {
    if (
      environment.production === false &&
      (qrCodeValue === '5e94cb5a63f0ff677e5a1234' ||
        qrCodeValue === '5e94cb5a63f0ff677e51234')
    ) {
      return of({
        createdDate: new Date(),
        eventId: '5e94cb5a63f0ff677e5a2691',
        id: qrCodeValue,
        isValid: qrCodeValue === '5e94cb5a63f0ff677e51234',
        lastModifiedDate: new Date(),
        subscriptionId: '5e94c8e3d7183943674bdd0d'
      })
    }

    return this.httpClient
      .put<Ticket>(
        this.invalidateTicketApiUrl,
        { qrCodeValue, eventId },
        this.applicationContext.createWithAuthorizationHeaders()
      )
      .pipe(
        catchError((response: HttpErrorResponse) =>
          throwError(
            LocalizedErrorMessage.getErrorMessageFromResponse.call(
              TicketInvalidationError,
              response
            )
          )
        )
      )
  }
}

import { Injectable } from '@angular/core'
import { HttpClient, HttpErrorResponse } from '@angular/common/http'
import { ApplicationContextService } from '../../common/service/application-context.service'
import { Event } from '../Event'
import { catchError } from 'rxjs/operators'
import { throwError } from 'rxjs'
import { LocalizedErrorMessage } from '../../common/LocalizedErrorMessage'

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private readonly getEventsApiUrl

  constructor(
    private httpClient: HttpClient,
    private applicationContext: ApplicationContextService
  ) {
    this.getEventsApiUrl = `${applicationContext.apiServerUrl}/event`
  }

  getEvents() {
    return this.httpClient
      .get<Event[]>(this.getEventsApiUrl)
      .pipe(
        catchError((response: HttpErrorResponse) =>
          throwError(
            LocalizedErrorMessage.getErrorMessageFromResponse(response)
          )
        )
      )
  }

  get(eventId: string) {
    return this.httpClient
      .get<Event>(`${this.getEventsApiUrl}/${eventId}`)
      .pipe(
        catchError((response: HttpErrorResponse) =>
          throwError(
            LocalizedErrorMessage.getErrorMessageFromResponse(response)
          )
        )
      )
  }
}

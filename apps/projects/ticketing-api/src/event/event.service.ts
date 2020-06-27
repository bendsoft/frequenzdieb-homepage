import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { ApiContextService } from '../api-context.service'
import { Event } from '../@types/event'
import { Concert } from '../@types/concert'
import { catchServerError } from '../common/error-message-handler.service'

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private readonly getEventsApiUrl
  private readonly serverErrorCatcher

  constructor(private httpClient: HttpClient, private apiContext: ApiContextService) {
    this.getEventsApiUrl = `${apiContext.apiServerUrl}/event`
    this.serverErrorCatcher = catchServerError()
  }

  getAll() {
    return this.httpClient.get<Event[]>(this.getEventsApiUrl).pipe(this.serverErrorCatcher)
  }

  get(eventId: string) {
    return this.httpClient
      .get<Event>(`${this.getEventsApiUrl}/${eventId}`)
      .pipe(this.serverErrorCatcher)
  }

  getConcert(concertId: string) {
    return this.httpClient
      .get<Concert>(`${this.getEventsApiUrl}/concert/${concertId}`)
      .pipe(this.serverErrorCatcher)
  }
}

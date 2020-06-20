import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { ApiContextService } from '../api-context.service'
import { Event } from '../@types/event'
import { Concert } from '../@types/concert'

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private readonly getEventsApiUrl

  constructor(
    private httpClient: HttpClient,
    private apiContext: ApiContextService
  ) {
    this.getEventsApiUrl = `${apiContext.apiServerUrl}/event`
  }

  getAll() {
    return this.httpClient
      .get<Event[]>(this.getEventsApiUrl)
      .pipe(this.apiContext.translateServerError())
  }

  get(eventId: string) {
    return this.httpClient
      .get<Event>(`${this.getEventsApiUrl}/${eventId}`)
      .pipe(this.apiContext.translateServerError())
  }

  getConcert(concertId: string) {
    return this.httpClient
      .get<Concert>(`${this.getEventsApiUrl}/concert/${concertId}`)
      .pipe(this.apiContext.translateServerError())
  }
}

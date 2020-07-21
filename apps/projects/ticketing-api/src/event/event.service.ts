import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { ApiContextService, catchServerError } from '../api-context.service'
import { Event } from '../@types/event'
import { Concert } from '../@types/concert'

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private get eventsRoute() {
    return `${this.apiContext.getApiServer()}/event`
  }

  private readonly serverErrorCatcher

  constructor(private httpClient: HttpClient, private apiContext: ApiContextService) {
    this.serverErrorCatcher = catchServerError()
  }

  getAll() {
    return this.httpClient.get<Event[]>(this.eventsRoute).pipe(this.serverErrorCatcher)
  }

  get(eventId: string): Observable<Event> {
    return this.httpClient
      .get<Event>(`${this.eventsRoute}/${eventId}`)
      .pipe(this.serverErrorCatcher)
  }

  getConcert(concertId: string): Observable<Concert> {
    return this.httpClient
      .get<Concert>(`${this.eventsRoute}/concert/${concertId}`)
      .pipe(this.serverErrorCatcher)
  }
}

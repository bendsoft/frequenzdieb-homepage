import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs'
import { ApiContextService } from '../api-context.service'
import { Ticket } from '../@types/ticket'
import { defaultErrorTranslator } from '../common/ErrorMessageHandler'

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private readonly ticketRoute = `${this.apiContext.apiServerUrl}/ticket/`

  constructor(
    private httpClient: HttpClient,
    private apiContext: ApiContextService
  ) {}

  invalidate(qrCodeValue: string, eventId: string): Observable<Ticket> {
    return this.httpClient
      .put<Ticket>(
        `${this.ticketRoute}/invalidate`,
        { qrCodeValue, eventId },
        this.apiContext.createWithAuthorizationHeaders()
      )
      .pipe(defaultErrorTranslator())
  }

  createPayment(
    ticketId,
    paymentInformation: {
      amount: number
      currency: string
    }
  ) {
    return this.httpClient
      .post<Ticket>(`${this.ticketRoute}/${ticketId}/pay`, paymentInformation)
      .pipe(defaultErrorTranslator())
  }
}

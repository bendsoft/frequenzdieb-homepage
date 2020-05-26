import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  api_url = 'https://dev-api.frequenzdieb.ch'

  constructor(private http: HttpClient) {}

  getBlogEntries() {
    return this.http.get(`${this.api_url}/blog`, {
      // headers: this.headers
    })
  }

  getBlogEntry(id) {
    return this.http.get(`${this.api_url}/blog/${id}`)
  }

  getConcert() {
    return this.http.get(
      `${this.api_url}/event/concert/5e94cb5a63f0ff677e5a2691`
    )
  }

  getTicketSignature(ticketId, amount, currency) {
    return this.http.post(`${this.api_url}/ticketing/${ticketId}/pay`, {
      amount,
      currency
    })
  }

  createTicket() {}
}

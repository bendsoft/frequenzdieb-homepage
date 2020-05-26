import { Injectable } from '@angular/core'
import { BehaviorSubject, Subject } from 'rxjs'
import { HttpHeaders } from '@angular/common/http'
import { Event } from '../../event/Event'
import { Ticket } from '../../ticketing/Ticket'
import { ScannedTicketLogEntry } from './ScannedTicketLogEntry'
import { LogUtil } from './LogUtil'

@Injectable({
  providedIn: 'root'
})
export class ApplicationContextService {
  apiServerUrl = 'https://dev-api.frequenzdieb.ch'
  // apiServerUrl = 'http://localhost:8085/api'

  db = window.localStorage

  scannedTicketsLog$ = new Subject<ScannedTicketLogEntry>()
  isAuthenticated = new BehaviorSubject(false)

  login(token) {
    this.db.setItem('jwt', token)
    this.isAuthenticated.next(true)
  }

  logout() {
    this.db.removeItem('jwt')
    this.isAuthenticated.next(false)
  }

  createWithAuthorizationHeaders(): Partial<{ headers?: HttpHeaders }> {
    return {
      headers: new HttpHeaders(this.createBearerHeader())
    }
  }

  createBearerHeader() {
    return {
      'Content-Type': 'application/json; charset=utf-8',
      Authorization: `Bearer ${this.db.getItem('jwt')}`
    }
  }

  setEvent(event: Event) {
    this.db.setItem('event', JSON.stringify(event))
  }

  getEvent(): Event {
    return JSON.parse(this.db.getItem('event'))
  }

  addScannedTicketLog(ticket: Ticket, ticketCheckResult: boolean) {
    const newLogEntry = LogUtil.createLogEntryFromTicketScan(
      ticket,
      ticketCheckResult
    )

    this.scannedTicketsLog$.next(newLogEntry)
    this.db.setItem(
      'log',
      JSON.stringify(
        LogUtil.addLogEntry(this.getScannedTicketLog(), newLogEntry)
      )
    )
  }

  getScannedTicketLog(): ScannedTicketLogEntry[] {
    return JSON.parse(this.db.getItem('log')) || []
  }

  resetScannedTicketLog() {
    this.db.removeItem('log')
  }
}

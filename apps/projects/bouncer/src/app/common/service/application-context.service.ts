import { Inject, Injectable } from '@angular/core'
import { Subject } from 'rxjs'
import {
  ApiContextService,
  BROWSER_STORAGE,
  Event,
  Ticket
} from '@bendsoft/ticketing-api'
import { ScannedTicketLogEntry } from './ScannedTicketLogEntry'
import { LogUtil } from './LogUtil'

@Injectable({
  providedIn: 'root'
})
export class ApplicationContextService {
  scannedTicketsLog$ = new Subject<ScannedTicketLogEntry>()

  constructor(
    @Inject(BROWSER_STORAGE) public db: Storage,
    @Inject(ApiContextService) public apiContext: ApiContextService
  ) {}

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

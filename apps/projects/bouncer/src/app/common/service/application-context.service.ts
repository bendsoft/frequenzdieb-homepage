import { Inject, Injectable } from '@angular/core'
import { Subject } from 'rxjs'
import { ApiContextService, BROWSER_STORAGE, Event, Ticket } from '@bendsoft/ticketing-api'
import { ScannedTicketLogEntry } from './ScannedTicketLogEntry'
import { LogUtil } from './LogUtil'

@Injectable({
  providedIn: 'root'
})
export class ApplicationContextService {
  scannedTicketsLog$ = new Subject<ScannedTicketLogEntry>()
  private readonly EVENT_TOPIC = 'event'
  private readonly LOG_TOPIC = 'log'

  constructor(
    @Inject(BROWSER_STORAGE) public db: Storage,
    @Inject(ApiContextService) public apiContext: ApiContextService
  ) {}

  setEvent(event: Event) {
    this.db.setItem(this.EVENT_TOPIC, JSON.stringify(event))
  }

  getEvent(): Event {
    return JSON.parse(this.db.getItem(this.EVENT_TOPIC))
  }

  addScannedTicketLog(ticket: Ticket, ticketCheckResult: boolean) {
    const newLogEntry = LogUtil.createLogEntryFromTicketScan(ticket, ticketCheckResult)

    this.scannedTicketsLog$.next(newLogEntry)
    this.db.setItem(
      this.LOG_TOPIC,
      JSON.stringify(LogUtil.addLogEntry(this.getScannedTicketLog(), newLogEntry))
    )
  }

  getScannedTicketLog(): ScannedTicketLogEntry[] {
    return JSON.parse(this.db.getItem(this.LOG_TOPIC)) || []
  }

  resetScannedTicketLog() {
    this.db.removeItem(this.LOG_TOPIC)
  }
}

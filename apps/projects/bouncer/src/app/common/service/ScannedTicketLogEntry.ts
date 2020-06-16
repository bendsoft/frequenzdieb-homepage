import { Ticket } from '@bendsoft/ticketing-api'

export interface ScannedTicketLogEntry {
  date: Date
  ticket: Ticket
  checkResult: boolean
}

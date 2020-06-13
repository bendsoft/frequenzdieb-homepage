import { Ticket } from '../../../../../ticketing-api/src/ticketing/Ticket'

export interface ScannedTicketLogEntry {
  date: Date
  ticket: Ticket
  checkResult: boolean
}

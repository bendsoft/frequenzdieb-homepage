import { Ticket } from '../../ticketing/Ticket'

export interface ScannedTicketLogEntry {
  date: Date
  ticket: Ticket
  checkResult: boolean
}

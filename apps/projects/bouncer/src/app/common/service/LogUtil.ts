import { Ticket } from '../../ticketing/Ticket'
import { ScannedTicketLogEntry } from './ScannedTicketLogEntry'

export class LogUtil {
  public static createLogEntryFromTicketScan(
    ticket: Ticket,
    checkResult: boolean
  ): ScannedTicketLogEntry {
    return {
      date: new Date(),
      ticket,
      checkResult
    }
  }

  public static addLogEntry(
    existingLogEntries: ScannedTicketLogEntry[] = [],
    newEntry: ScannedTicketLogEntry,
    maxSize = 100
  ) {
    const maxSizeLogEntries = existingLogEntries.slice(0, maxSize - 1)
    maxSizeLogEntries.unshift(newEntry)
    return maxSizeLogEntries
  }
}

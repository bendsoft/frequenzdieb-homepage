import { LocalizedErrorMessage } from '../common/LocalizedErrorMessage'

export class TicketInvalidationError extends LocalizedErrorMessage {
  public static NOT_FOUND = $localize`:@@NotFoundTicketInvalidationErrorMsg:Ticket could not be found`
  public static ALREADY_USED = $localize`:@@AlreadyUsedTicketInvalidationErrorMsg:The Ticket has already been used`
  public static NOT_PAYED = $localize`:@@NotPayedTicketInvalidationErrorMsg:The Ticket lacks a valid Payment`
  public static ANOTHER_EVENT = $localize`:@@AnotherEventTicketInvalidationErrorMsg:The Ticket belongs to another Event`
}

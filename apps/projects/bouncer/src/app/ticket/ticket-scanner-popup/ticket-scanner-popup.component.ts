import { Component, Inject } from '@angular/core'
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog'
import { Observable } from 'rxjs'
import { HttpErrorResponse } from '@angular/common/http'
import { Ticket } from '@bendsoft/ticketing-api'

@Component({
  selector: 'bncr-ticket-scanner-popup',
  templateUrl: './ticket-scanner-popup.component.html',
  styleUrls: ['./ticket-scanner-popup.component.scss']
})
export class TicketScannerPopupComponent {
  isFinished = false
  isTicketCheckSuccessful = null
  private scannedTicket: Ticket | null

  invalidTicketReason: string

  constructor(
    @Inject(MAT_DIALOG_DATA) ticketInvalidationProcess: Observable<Ticket>,
    private dialogRef: MatDialogRef<TicketScannerPopupComponent>
  ) {
    ticketInvalidationProcess.subscribe(
      (ticket) => this.onTicketValid(ticket),
      (response: HttpErrorResponse) => this.onTicketInvalid(response),
      () => {
        this.isFinished = true
      }
    )
  }

  private onTicketValid(ticket: Ticket) {
    this.scannedTicket = ticket
    this.isTicketCheckSuccessful = true
  }

  private onTicketInvalid(response: HttpErrorResponse, ticket?) {
    this.scannedTicket = ticket
    this.isTicketCheckSuccessful = false
    this.invalidTicketReason = response.message
  }

  closePopup() {
    this.dialogRef.close({
      ticket: this.scannedTicket,
      ticketCheckResult: this.isTicketCheckSuccessful
    })
  }
}

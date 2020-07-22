import { AfterViewInit, Component } from '@angular/core'
import { Router } from '@angular/router'
import { MatDialog } from '@angular/material/dialog'
import { Event, TicketService } from '@bendsoft/ticketing-api'
import { isEmpty } from 'lodash-es'
import { of } from 'rxjs'
import { TicketScannerPopupComponent } from '../ticket-scanner-popup/ticket-scanner-popup.component'
import { ApplicationContextService } from '../../common/service/application-context.service'

@Component({
  selector: 'bncr-ticket-scanner',
  templateUrl: './ticket-scanner.component.html',
  styleUrls: ['./ticket-scanner.component.scss']
})
export class TicketScannerComponent implements AfterViewInit {
  isScannerReady = false
  isAuthenticationValid = false

  eventToCheckTicketsFor: Event
  isScanningEnabled = true

  constructor(
    private ticketingService: TicketService,
    private router: Router,
    private dialog: MatDialog,
    private applicationContext: ApplicationContextService
  ) {
    applicationContext.apiContext.isAuthenticated.subscribe((isAuthenticated) => {
      this.isAuthenticationValid = isAuthenticated
    })

    this.eventToCheckTicketsFor = applicationContext.getEvent()
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.isScannerReady = true
    }, 1000)
  }

  isReady() {
    return this.isScannerReady && this.isAuthenticationValid
  }

  scanSuccessHandler(scannedValue) {
    if (!this.isScanningEnabled) {
      return
    }
    this.isScanningEnabled = false

    const ticketInvalidationProcess = this.ticketingService.invalidate(
      scannedValue,
      this.eventToCheckTicketsFor.id
    )

    this.dialog
      .open(TicketScannerPopupComponent, {
        width: '300px',
        height: '300px',
        hasBackdrop: true,
        data: ticketInvalidationProcess
      })
      .afterClosed()
      .subscribe((checkResult) => {
        const ticket$ = isEmpty(checkResult.ticket)
          ? this.ticketingService.get(atob(scannedValue))
          : of(checkResult.ticket)

        ticket$.subscribe(
          (ticket) => {
            this.applicationContext.addScannedTicketLog(ticket, checkResult.ticketCheckResult)
          },
          (error) => console.log(error),
          () => {
            this.isScanningEnabled = true
          }
        )
      })
  }
}

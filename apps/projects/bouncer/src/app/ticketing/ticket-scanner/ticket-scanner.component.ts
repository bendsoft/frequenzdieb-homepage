import { AfterViewInit, Component } from '@angular/core'
import { Router } from '@angular/router'
import { MatDialog } from '@angular/material/dialog'
import { TicketingService } from '../service/ticketing.service'
import { TicketScannerPopupComponent } from '../ticket-scanner-popup/ticket-scanner-popup.component'
import { ApplicationContextService } from '../../common/service/application-context.service'
import { Event } from '../../event/Event'
import { environment } from '../../../environments/environment'

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
    private ticketingService: TicketingService,
    private router: Router,
    private dialog: MatDialog,
    private applicationContext: ApplicationContextService
  ) {
    applicationContext.isAuthenticated.subscribe((isAuthenticated) => {
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

    const ticketInvalidationProcess = this.ticketingService.invalidate(
      scannedValue,
      this.eventToCheckTicketsFor.id
    )

    this.isScanningEnabled = false
    this.dialog
      .open(TicketScannerPopupComponent, {
        width: '300px',
        height: '300px',
        hasBackdrop: true,
        data: ticketInvalidationProcess
      })
      .afterClosed()
      .subscribe((checkResult) => {
        this.isScanningEnabled = true
        this.applicationContext.addScannedTicketLog(
          checkResult.ticket || { id: atob(scannedValue) },
          checkResult.ticketCheckResult
        )
      })
  }

  showTestButton() {
    return this.isScannerReady && !environment.production
  }
}

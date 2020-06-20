import { Component, Inject, LOCALE_ID, OnDestroy } from '@angular/core'
import { animate, state, style, transition, trigger } from '@angular/animations'
import { forkJoin, Subscription as RxSubscription } from 'rxjs'
import { get, isEmpty } from 'lodash'
import { formatDate } from '@angular/common'
import {
  Event,
  EventService,
  Subscription,
  SubscriptionService
} from '@bendsoft/ticketing-api'
import { ApplicationContextService } from '../../common/service/application-context.service'
import { ScannedTicketLogEntry } from '../../common/service/ScannedTicketLogEntry'
import { LogUtil } from '../../common/service/LogUtil'

enum LogEntryDetailsState {
  COLLAPSED = 'collapsed',
  EXPANDED = 'expanded'
}

interface LogEntryDetails {
  subscription?: Subscription
  event?: Event
  isLoading: boolean
  animationState: LogEntryDetailsState
  detailsLoadingSubscription: RxSubscription
}

@Component({
  selector: 'bncr-scanner-log',
  templateUrl: './scanner-log.component.html',
  styleUrls: ['./scanner-log.component.scss'],
  animations: [
    trigger('detailExpand', [
      state(
        LogEntryDetailsState.COLLAPSED,
        style({ height: '0px', minHeight: '0' })
      ),
      state(LogEntryDetailsState.EXPANDED, style({ height: '*' })),
      transition(
        'expanded <=> collapsed',
        animate('500ms cubic-bezier(0.4, 0.0, 0.2, 1)')
      )
    ])
  ]
})
export class ScannerLogComponent implements OnDestroy {
  scannedTicketsLog: ScannedTicketLogEntry[]
  columnsToDisplay = [
    $localize`:@@CheckedDateScannerLogTableColumn:Checked-date`,
    $localize`:@@TicketIdScannerLogTableColumn:Ticket-ID`,
    $localize`:@@ResultScannerLogTableColumn:Result`
  ]

  loadedDetailsForLogEntry = new WeakMap<
    ScannedTicketLogEntry,
    LogEntryDetails
  >()

  getAnimationState(logEntry: ScannedTicketLogEntry) {
    const loadedDetails = this.loadedDetailsForLogEntry.get(logEntry)
    if (!isEmpty(loadedDetails)) {
      return loadedDetails.animationState
    }

    return LogEntryDetailsState.COLLAPSED
  }

  isLoadingDetails(logEntry: ScannedTicketLogEntry) {
    if (this.loadedDetailsForLogEntry.has(logEntry)) {
      return this.loadedDetailsForLogEntry.get(logEntry).isLoading
    }

    return false
  }

  private logsSubscription

  constructor(
    private applicationContextService: ApplicationContextService,
    private eventService: EventService,
    private subscriptionService: SubscriptionService,
    @Inject(LOCALE_ID) private locale: string
  ) {
    this.scannedTicketsLog = this.applicationContextService.getScannedTicketLog()

    this.logsSubscription = this.applicationContextService.scannedTicketsLog$.subscribe(
      (logEntry) => {
        this.scannedTicketsLog = LogUtil.addLogEntry(
          this.scannedTicketsLog,
          logEntry
        )
      }
    )
  }

  ngOnDestroy() {
    this.logsSubscription.unsubscribe()
  }

  resetLog() {
    this.applicationContextService.resetScannedTicketLog()
    this.scannedTicketsLog = []
  }

  onLogEntryClicked(logEntry: ScannedTicketLogEntry) {
    if (
      isEmpty(logEntry.ticket.eventId) ||
      isEmpty(logEntry.ticket.subscriptionId)
    ) {
      return
    }

    if (this.loadedDetailsForLogEntry.has(logEntry)) {
      if (
        this.loadedDetailsForLogEntry.get(logEntry).animationState ===
        LogEntryDetailsState.EXPANDED
      ) {
        this.onCloseLogEntryDetails(logEntry)
      } else {
        this.onFinishedLoading(logEntry)
      }
    } else {
      const detailsLoadingSubscription = this.loadDetails(logEntry).subscribe(
        (result: { subscription: Subscription; event: Event }) => {
          const logEntryDetails = this.loadedDetailsForLogEntry.get(logEntry)
          logEntryDetails.subscription = result.subscription
          logEntryDetails.event = result.event
        },
        (error) => console.log(error),
        () => this.onFinishedLoading(logEntry)
      )

      this.loadedDetailsForLogEntry.set(logEntry, {
        isLoading: true,
        animationState: LogEntryDetailsState.COLLAPSED,
        detailsLoadingSubscription
      })
    }
  }

  private loadDetails(logEntry: ScannedTicketLogEntry) {
    return forkJoin({
      subscription: this.subscriptionService.get(
        logEntry.ticket.subscriptionId
      ),
      event: this.eventService.get(logEntry.ticket.eventId)
    })
  }

  private onFinishedLoading(logEntry: ScannedTicketLogEntry) {
    this.cancelPendingRequest(logEntry)
    this.loadedDetailsForLogEntry.get(logEntry).animationState =
      LogEntryDetailsState.EXPANDED
  }

  private onCloseLogEntryDetails(logEntry: ScannedTicketLogEntry) {
    this.loadedDetailsForLogEntry.get(logEntry).animationState =
      LogEntryDetailsState.COLLAPSED
  }

  private cancelPendingRequest(logEntry: ScannedTicketLogEntry) {
    const loadedDetails = this.loadedDetailsForLogEntry.get(logEntry)
    loadedDetails.isLoading = false
    loadedDetails.detailsLoadingSubscription.unsubscribe()
  }

  getLogEntryDetailProperty(logEntry: ScannedTicketLogEntry, property: string) {
    return get(this.loadedDetailsForLogEntry.get(logEntry), property, '')
  }

  getTicketStatus(logEntry: ScannedTicketLogEntry) {
    return this.getLogEntryDetailProperty(logEntry, 'logEntry.ticket.isValid')
      ? $localize`:@@ScannedTicketValidationStatusUnused:Unused`
      : $localize`:@@ScannedTicketValidationStatusVoid:Void`
  }

  formatDateByCurrentLocale(value: string | number | Date): string {
    if (isEmpty(value)) {
      return ''
    }

    return formatDate(value, 'short', this.locale, '+0200')
  }
}

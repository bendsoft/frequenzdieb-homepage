import { Component, Inject, LOCALE_ID, OnDestroy } from '@angular/core'
import { animate, state, style, transition, trigger } from '@angular/animations'
import { forkJoin, Observable, of, Subscription as RxSubscription } from 'rxjs'
import { get, isEmpty } from 'lodash-es'
import { formatDate } from '@angular/common'
import { Event, EventService, Subscription, SubscriptionService } from '@bendsoft/ticketing-api'
import { ApplicationContextService } from '../../common/service/application-context.service'
import { ScannedTicketLogEntry } from '../../common/service/ScannedTicketLogEntry'
import { LogUtil } from '../../common/service/LogUtil'

enum LogEntryRowState {
  COLLAPSED = 'collapsed',
  EXPANDED = 'expanded'
}

interface LogEntryDetails {
  subscription?: Subscription
  event?: Event
}

interface LogEntryDetailsState {
  isLoading: boolean
  animationState: LogEntryRowState
  detailsLoadingSubscription: RxSubscription
}

@Component({
  selector: 'bncr-scanner-log',
  templateUrl: './scanner-log.component.html',
  styleUrls: ['./scanner-log.component.scss'],
  animations: [
    trigger('detailExpand', [
      state(LogEntryRowState.COLLAPSED, style({ height: '0px', minHeight: '0' })),
      state(LogEntryRowState.EXPANDED, style({ height: '*' })),
      transition('expanded <=> collapsed', animate('500ms cubic-bezier(0.4, 0.0, 0.2, 1)'))
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
    LogEntryDetailsState & LogEntryDetails
  >()

  getAnimationState(logEntry: ScannedTicketLogEntry) {
    const loadedDetails = this.loadedDetailsForLogEntry.get(logEntry)
    if (!isEmpty(loadedDetails)) {
      return loadedDetails.animationState
    }

    return LogEntryRowState.COLLAPSED
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
        this.scannedTicketsLog = LogUtil.addLogEntry(this.scannedTicketsLog, logEntry)
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
    if (this.loadedDetailsForLogEntry.has(logEntry)) {
      if (
        this.loadedDetailsForLogEntry.get(logEntry).animationState === LogEntryRowState.EXPANDED
      ) {
        this.onCloseLogEntryDetails(logEntry)
      } else {
        this.onFinishedLoading(logEntry)
      }
    } else {
      this.loadedDetailsForLogEntry.set(logEntry, {
        isLoading: true,
        animationState: LogEntryRowState.COLLAPSED,
        detailsLoadingSubscription: RxSubscription.EMPTY,
        event: null,
        subscription: null
      })

      this.loadedDetailsForLogEntry.get(logEntry).detailsLoadingSubscription = this.loadDetails(
        logEntry
      ).subscribe(
        (result: LogEntryDetails) => this.updateLogEntryWithDetails(logEntry, result),
        (error) => console.log(error),
        () => this.onFinishedLoading(logEntry)
      )
    }
  }

  private updateLogEntryWithDetails(logEntry: ScannedTicketLogEntry, details: LogEntryDetails) {
    const entry = this.loadedDetailsForLogEntry.get(logEntry)
    entry.event = details.event
    entry.subscription = details.subscription
  }

  private loadDetails(logEntry: ScannedTicketLogEntry): Observable<LogEntryDetails> {
    if (
      isEmpty(logEntry.ticket.id) ||
      (isEmpty(logEntry.ticket.subscriptionId) && isEmpty(logEntry.ticket.eventId))
    ) {
      return of({})
    }
    const sources: Partial<{
      event: Observable<Event>
      subscription: Observable<Subscription>
    }> = {}

    if (!isEmpty(logEntry.ticket.eventId)) {
      sources.event = this.eventService.get(logEntry.ticket.eventId)
    }
    if (!isEmpty(logEntry.ticket.subscriptionId)) {
      sources.subscription = this.subscriptionService.get(logEntry.ticket.subscriptionId)
    }

    return forkJoin(sources)
  }

  private onFinishedLoading(logEntry: ScannedTicketLogEntry) {
    this.cancelPendingRequest(logEntry)
    this.loadedDetailsForLogEntry.get(logEntry).animationState = LogEntryRowState.EXPANDED
  }

  private onCloseLogEntryDetails(logEntry: ScannedTicketLogEntry) {
    this.loadedDetailsForLogEntry.get(logEntry).animationState = LogEntryRowState.COLLAPSED
  }

  private cancelPendingRequest(logEntry: ScannedTicketLogEntry) {
    const loadedDetails = this.loadedDetailsForLogEntry.get(logEntry)
    loadedDetails.isLoading = false
    loadedDetails.detailsLoadingSubscription.unsubscribe()
  }

  getLogEntryDetailProperty(logEntry: ScannedTicketLogEntry, property: string) {
    return get(this.loadedDetailsForLogEntry.get(logEntry), property, '')
  }

  getLogEntryProperty(logEntry: ScannedTicketLogEntry, property: string) {
    return get(logEntry, property, '')
  }

  getTicketStatus(logEntry: ScannedTicketLogEntry) {
    return this.getLogEntryDetailProperty(logEntry, 'logEntry.ticket.isValid')
      ? $localize`:@@ScannedTicketValidationStatusUnused:Unused`
      : $localize`:@@ScannedTicketValidationStatusVoid:Void`
  }

  formatDateByCurrentLocale(value: string): string {
    if (isEmpty(value)) {
      return ''
    }

    return formatDate(value, 'short', this.locale, '+0200')
  }
}

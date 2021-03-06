import { BrowserModule } from '@angular/platform-browser'
import { NgModule } from '@angular/core'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { FormsModule, ReactiveFormsModule } from '@angular/forms'
import { HttpClientModule } from '@angular/common/http'
import { RouterModule } from '@angular/router'
import { registerLocaleData } from '@angular/common'
import localeDeCH from '@angular/common/locales/de-CH'
import localeDeCHExtra from '@angular/common/locales/extra/de-CH'
import { MatListModule } from '@angular/material/list'
import { MatTableModule } from '@angular/material/table'
import { MatToolbarModule } from '@angular/material/toolbar'
import { MatSidenavModule } from '@angular/material/sidenav'
import { MatIconModule } from '@angular/material/icon'
import { MatButtonModule } from '@angular/material/button'
import { MatInputModule } from '@angular/material/input'
import { MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldModule } from '@angular/material/form-field'
import { MatExpansionModule } from '@angular/material/expansion'
import { ZXingScannerModule } from '@zxing/ngx-scanner'
import { MAT_DIALOG_DEFAULT_OPTIONS, MatDialogModule } from '@angular/material/dialog'
import { ServiceWorkerModule } from '@angular/service-worker'
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'
import { MatSelectModule } from '@angular/material/select'
import { MatProgressBarModule } from '@angular/material/progress-bar'
import { API_SERVER_URLS, DEFAULT_ERROR_MESSAGES } from '@bendsoft/ticketing-api'
import { SyncComponent } from './ticket/sync/sync.component'
import { ScannerLogComponent } from './ticket/scanner-log/scanner-log.component'
import { LogoutComponent } from './auth/logout/logout.component'
import { LoginComponent } from './auth/login/login.component'
import { TicketScannerPopupComponent } from './ticket/ticket-scanner-popup/ticket-scanner-popup.component'
import { environment } from '../environments/environment.prod'
import { AppRoutingModule } from './app-routing.module'
import { TicketScannerComponent } from './ticket/ticket-scanner/ticket-scanner.component'
import { BncrComponent } from './bncr.component'
import { SettingsComponent } from './settings/settings.component'

registerLocaleData(localeDeCH, 'de-CH', localeDeCHExtra)

@NgModule({
  declarations: [
    BncrComponent,
    LoginComponent,
    LogoutComponent,
    TicketScannerComponent,
    TicketScannerPopupComponent,
    ScannerLogComponent,
    SyncComponent,
    SettingsComponent
  ],
  imports: [
    BrowserModule.withServerTransition({ appId: 'serverApp' }),
    AppRoutingModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    ZXingScannerModule,
    HttpClientModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: environment.production
    }),
    RouterModule,
    MatSelectModule,
    MatProgressBarModule,
    MatTableModule
  ],
  providers: [
    {
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: { appearance: 'fill' }
    },
    { provide: MAT_DIALOG_DEFAULT_OPTIONS, useValue: { hasBackdrop: false } },
    {
      provide: DEFAULT_ERROR_MESSAGES,
      useValue: {
        NOT_AUTHORIZED: 'Die Anmeldung ist fehlgeschlagen. Bitte überprüfe das Passwort',
        INVALID_REQUEST: 'Mit der Anfrage stimmt etwas nicht',
        UNKNOWN_ERROR: 'Ein unbekannter Fehler ist aufgetreten',
        SERVER_ERROR: 'Ein Fehler ist auf den Server aufgetreten',
        NOT_FOUND: 'Nicht gefunden',
        SIGNATURE_INVALID: 'Signatur ist ungültig',
        VALIDATION_ERROR: 'Die Validierung ist fehlgeschlagen',
        ENTITY_INVALID: 'Der gelieferte Datensatz ist ungültig',
        EMAIL_INVALID: 'Die E-Mail Adresse ist ungültig',
        TICKET_MISSING_SUBSCRIPTION:
          'Die zum Ticket gehörige Subscription konnte nicht ermittelt werden',
        TICKET_ID_INVALID: 'Ungültige Ticket-ID',
        TICKET_ALREADY_USED: 'Das Ticket wurde bereits benutzt',
        TICKET_FOR_ANOTHER_EVENT: 'Das Ticket ist für einen anderen Event',
        TICKET_NOT_PAID: 'Es konnte keine passende Zahlung für das Ticket gefunden werden',
        SUBSCRIPTION_INVALID_ID: 'Ungültige Subscription-ID',
        SUBSCRIPTION_NOT_EXISTS: 'Diese Subscription konnte nicht gefunden werden'
      }
    },
    {
      provide: API_SERVER_URLS,
      useValue: (environment.production
        ? ['https://dev-api.frequenzdieb.ch', 'https://api.frequenzdieb.ch']
        : ['https://api.frequenzdieb.ch', 'https://dev-api.frequenzdieb.ch']
      ).concat('http://localhost:8085/api')
    }
  ],
  bootstrap: [BncrComponent]
})
export class AppModule {}

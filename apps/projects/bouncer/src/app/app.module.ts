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
import {
  MAT_FORM_FIELD_DEFAULT_OPTIONS,
  MatFormFieldModule
} from '@angular/material/form-field'
import { MatExpansionModule } from '@angular/material/expansion'
import { ZXingScannerModule } from '@zxing/ngx-scanner'
import {
  MAT_DIALOG_DEFAULT_OPTIONS,
  MatDialogModule
} from '@angular/material/dialog'
import { ServiceWorkerModule } from '@angular/service-worker'
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'
import { MatSelectModule } from '@angular/material/select'
import { MatProgressBarModule } from '@angular/material/progress-bar'
import { SyncComponent } from './ticketing/sync/sync.component'
import { ScannerLogComponent } from './ticketing/scanner-log/scanner-log.component'
import { LogoutComponent } from './auth/logout/logout.component'
import { TicketScannerPopupComponent } from './ticketing/ticket-scanner-popup/ticket-scanner-popup.component'
import { environment } from '../environments/environment.prod'
import { LoginComponent } from './auth/login/login.component'
import { AppRoutingModule } from './app-routing.module'
import { TicketScannerComponent } from './ticketing/ticket-scanner/ticket-scanner.component'

import { BncrComponent } from './bncr.component'

registerLocaleData(localeDeCH, 'de-CH', localeDeCHExtra)

@NgModule({
  declarations: [
    BncrComponent,
    LoginComponent,
    LogoutComponent,
    TicketScannerComponent,
    TicketScannerPopupComponent,
    ScannerLogComponent,
    SyncComponent
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
    { provide: MAT_DIALOG_DEFAULT_OPTIONS, useValue: { hasBackdrop: false } }
  ],
  bootstrap: [BncrComponent]
})
export class AppModule {}
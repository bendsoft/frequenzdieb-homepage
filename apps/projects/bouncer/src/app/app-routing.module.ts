import { NgModule } from '@angular/core'
import { RouterModule, Routes } from '@angular/router'
import { LoginComponent } from './auth/login/login.component'
import { TicketScannerComponent } from './ticket/ticket-scanner/ticket-scanner.component'
import { SyncComponent } from './ticket/sync/sync.component'
import { LogoutComponent } from './auth/logout/logout.component'
import { ScannerLogComponent } from './ticket/scanner-log/scanner-log.component'

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'logout', component: LogoutComponent },
  { path: 'scan', component: TicketScannerComponent },
  { path: 'sync', component: SyncComponent },
  { path: 'logs', component: ScannerLogComponent },
  { path: '**', redirectTo: '/scan' }
]

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {}

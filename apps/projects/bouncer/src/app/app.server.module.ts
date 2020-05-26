import { NgModule } from '@angular/core'
import { ServerModule } from '@angular/platform-server'

import { RouterModule, Routes } from '@angular/router'
import { AppModule } from './app.module'
import { AppShellComponent } from './app-shell/app-shell.component'
import { BncrComponent } from './bncr.component'

const routes: Routes = [{ path: 'shell', component: AppShellComponent }]

@NgModule({
  imports: [AppModule, ServerModule, RouterModule.forRoot(routes)],
  bootstrap: [BncrComponent],
  declarations: [AppShellComponent]
})
export class AppServerModule {}

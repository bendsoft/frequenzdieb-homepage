import { Component, ViewChild } from '@angular/core'
import { NavigationEnd, NavigationStart, Router } from '@angular/router'
import { filter } from 'rxjs/operators'
import { MatSidenav } from '@angular/material/sidenav'
import { ApplicationContextService } from './common/service/application-context.service'
import { getAllNavRoutes, NavRoute } from './NavRoute'

@Component({
  selector: 'bncr-root',
  templateUrl: './bncr.component.html',
  styleUrls: ['./bncr.component.scss']
})
export class BncrComponent {
  title = 'frequenzdieb-bouncer'

  isInSync = true
  isAuthenticationValid = false

  isNavLogoutDisabled = true
  isNavScannerDisabled = true
  isNavSyncDisabled = true
  isNavLogsDisabled = true

  @ViewChild(MatSidenav) sideNavComponent

  NavRoute = NavRoute

  constructor(
    private applicationContext: ApplicationContextService,
    private router: Router
  ) {
    applicationContext.apiContext.isAuthenticated.subscribe(
      (isAuthenticated) => {
        this.isAuthenticationValid = isAuthenticated
      }
    )

    router.events
      .pipe(filter((event) => event instanceof NavigationStart))
      .subscribe((event: NavigationStart) => {
        if (event.url !== NavRoute.LOGIN) {
          if (
            applicationContext.apiContext.isAuthenticated.getValue() ===
              false ||
            !applicationContext.getEvent()
          ) {
            router.navigateByUrl(NavRoute.LOGIN)
          }
        }
      })

    router.events
      .pipe(
        filter(
          (event: NavigationEnd) =>
            event instanceof NavigationEnd &&
            getAllNavRoutes().includes(event.url)
        )
      )
      .subscribe((navEvent: NavigationEnd) => {
        this.sideNavComponent.close()

        this.isNavLogoutDisabled =
          !this.isAuthenticationValid ||
          navEvent.url === NavRoute.LOGIN ||
          navEvent.url === NavRoute.LOGOUT

        this.isNavSyncDisabled =
          !this.isAuthenticationValid ||
          navEvent.url === NavRoute.SYNC ||
          !this.isInSync

        this.isNavScannerDisabled =
          !this.isAuthenticationValid || navEvent.url === NavRoute.SCAN

        this.isNavLogsDisabled =
          !this.isAuthenticationValid || navEvent.url === NavRoute.LOGS
      })
  }
}

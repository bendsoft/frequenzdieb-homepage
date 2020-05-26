import { Component } from '@angular/core'
import { Router } from '@angular/router'
import { interval } from 'rxjs'
import { map, take } from 'rxjs/operators'
import { ApplicationContextService } from '../../common/service/application-context.service'
import { NavRoute } from '../../NavRoute'

@Component({
  selector: 'bncr-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss']
})
export class LogoutComponent {
  private readonly redirectDelayInSeconds = 5
  private readonly redirectCountdown = interval(1000).pipe(
    take(this.redirectDelayInSeconds),
    map((num) => this.redirectDelayInSeconds - 1 - num)
  )
  secondsLeft = this.redirectDelayInSeconds

  isLogoutSuccessful = false

  errorMsg

  constructor(router: Router, applicationContext: ApplicationContextService) {
    if (applicationContext.isAuthenticated.getValue()) {
      applicationContext.logout()
      this.isLogoutSuccessful = true
      this.startRedirectCountdown(router)
    }
  }

  private startRedirectCountdown(router: Router) {
    this.redirectCountdown.subscribe(
      (countdown) => {
        this.secondsLeft = countdown
      },
      (error) => {
        this.errorMsg = error
      },
      () => router.navigateByUrl(NavRoute.LOGIN)
    )
  }
}

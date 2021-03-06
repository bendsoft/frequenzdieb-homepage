import { Component } from '@angular/core'
import { FormControl, Validators } from '@angular/forms'
import { Router } from '@angular/router'
import { ApiErrorStateMatcher, Event, EventService, LoginService } from '@bendsoft/ticketing-api'
import { HttpErrorResponse } from '@angular/common/http'
import { ApplicationContextService } from '../../common/service/application-context.service'

@Component({
  selector: 'bncr-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  hide = true

  passwordInput = new FormControl('')

  events: Event[] = []
  loadingEvents = false
  eventSelect = new FormControl({
    value: '',
    disabled: true
  })

  loginErrorState = new ApiErrorStateMatcher(true, this.passwordInput)
  eventsLoadingErrorState = new ApiErrorStateMatcher(false, this.eventSelect, Validators.required)

  constructor(
    private router: Router,
    private loginService: LoginService,
    private eventService: EventService,
    private applicationContext: ApplicationContextService
  ) {
    if (this.events.length === 0) {
      this.loadingEvents = true
      this.eventService.getAll().subscribe(
        (events: Event[]) => {
          this.events = events
          this.onFinishedLoadingEvents()
        },
        (response: HttpErrorResponse) => {
          this.onFinishedLoadingEvents()
          this.eventsLoadingErrorState.update(response.error)
        }
      )
    }
  }

  private onFinishedLoadingEvents() {
    this.loadingEvents = false
    this.eventSelect.enable()
  }

  isLoginPossible() {
    return (
      this.passwordInput.value && this.passwordInput.value.length > 0 && !!this.eventSelect.value
    )
  }

  login() {
    if (!this.isLoginPossible()) return

    this.loginService
      .login({
        username: 'admin',
        password: this.passwordInput.value
      })
      .subscribe(
        () => {
          this.applicationContext.setEvent(this.eventSelect.value)
          this.router.navigateByUrl('/scan')
        },
        (response: HttpErrorResponse) => this.loginErrorState.update(response.error)
      )
  }
}

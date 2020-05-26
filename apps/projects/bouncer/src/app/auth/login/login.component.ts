import { Component, OnInit } from '@angular/core'
import { FormControl, Validators } from '@angular/forms'
import { Router } from '@angular/router'
import { LoginService } from '../service/login.service'
import { Event } from '../../event/Event'
import { EventService } from '../../event/service/event.service'
import { ApplicationContextService } from '../../common/service/application-context.service'

@Component({
  selector: 'bncr-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  hide = true
  passwordInput = new FormControl('')

  events: Event[] = []
  loadingEvents = false
  eventSelect = new FormControl(
    {
      value: '',
      disabled: true
    },
    Validators.required
  )

  constructor(
    private router: Router,
    private loginService: LoginService,
    private eventService: EventService,
    private applicationContextService: ApplicationContextService
  ) {}

  ngOnInit(): void {
    if (this.events.length === 0) {
      this.loadingEvents = true
      this.eventService.getEvents().subscribe((events) => {
        this.events = events
        this.loadingEvents = false
        this.eventSelect.enable()
      })
    }
  }

  isLoginPossible() {
    return (
      this.passwordInput.value &&
      this.passwordInput.value.length > 0 &&
      !!this.eventSelect.value
    )
  }

  login() {
    if (!this.isLoginPossible()) return

    this.applicationContextService.setEvent(this.eventSelect.value)

    this.loginService
      .login({
        username: 'admin',
        password: this.passwordInput.value
      })
      .subscribe(() => {
        this.router.navigateByUrl('/scan')
      })
  }
}

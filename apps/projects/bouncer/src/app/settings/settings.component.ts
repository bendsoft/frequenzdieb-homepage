import { Component } from '@angular/core'
import { MatSelectChange } from '@angular/material/select'
import { ApplicationContextService } from '../common/service/application-context.service'

@Component({
  selector: 'bncr-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {
  selectedHost = this.applicationContext.apiContext.getApiServer()

  constructor(public applicationContext: ApplicationContextService) {}

  onChangeHost(event: MatSelectChange) {
    this.applicationContext.apiContext.setApiServer(event.value)
  }
}

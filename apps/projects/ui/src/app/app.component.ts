import { Component } from '@angular/core'
import { ApiService } from './service/common/api/api.service'

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'frequenzdieb-ui'

  constructor(private api: ApiService) {}
}

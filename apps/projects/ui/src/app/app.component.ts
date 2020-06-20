import { Component } from '@angular/core'
import { ApiService } from './common/api/api.service'

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'frequenzdieb-ui'

  constructor(private api: ApiService) {}
}

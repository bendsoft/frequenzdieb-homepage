import { Component, OnInit } from '@angular/core'

import { OpenPopupsService } from '../common/popup/open-popups.service'

declare var $: any

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  mobileNavigationOpen: Boolean = false

  constructor(public popups: OpenPopupsService) {}

  ngOnInit() {}

  toggleMobileNavigation() {
    this.mobileNavigationOpen = !this.mobileNavigationOpen
  }

  closeMobileNavigation() {
    this.mobileNavigationOpen = false
  }
}

import { Component, OnDestroy, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { FormControl } from '@angular/forms'

import { SubscriptionService } from 'projects/ui/src/app/service/subscription/subscription.service'

import { OpenPopupsService } from '../../../service/common/popup/open-popups.service'

import { Popup } from '../../../service/common/popup/open-popups.interface'

@Component({
  selector: 'app-newsletter-sub',
  templateUrl: './newsletter-sub.component.html',
  styleUrls: ['./newsletter-sub.component.scss']
})
export class NewsletterSubComponent implements OnInit, Popup, OnDestroy {
  private readonly directNewsletterSubscriptionUrl = '/newsletter/subscribe'
  private readonly popupName = 'newsletter'

  email = new FormControl('')

  isOpen: boolean

  constructor(
    private subscriptionApi: SubscriptionService,
    public popups: OpenPopupsService,
    private route: Router
  ) {}

  ngOnInit() {
    this.popups.addPopup(this.popupName, this)
  }

  ngOnDestroy() {
    this.popups.removePopup(this.popupName)
  }

  closePopup() {
    this.popups.setPopupOpen('newsletter', false)
    if (
      this.isOpen === false &&
      this.route.url === this.directNewsletterSubscriptionUrl
    ) {
      this.route.navigateByUrl('/home')
    }
    this.email.reset()
  }

  submitNewsletterSub() {
    this.subscriptionApi
      .createSubscription(this.email.value, '', '', true)
      .subscribe((data) => {
        console.log(data)
      })
    this.closePopup()
  }
}

import { AfterViewInit, Component } from '@angular/core'
import { ActivatedRoute, Router } from '@angular/router'

import { clone } from 'lodash-es'
import { SubscriptionService } from '@bendsoft/ticketing-api'
import { ApiService } from '../../../common/api/api.service'

@Component({
  selector: 'app-newsletter-unsubscribe',
  templateUrl: './newsletter-unsubscribe.component.html',
  styleUrls: ['./newsletter-unsubscribe.component.scss']
})
export class NewsletterUnsubscribeComponent implements AfterViewInit {
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private api: ApiService,
    private subscription: SubscriptionService
  ) {}

  animationTime = 4
  confirmationSuccessful: boolean
  progressLoading = false
  subscriptionId: string

  ngAfterViewInit(): void {
    this.route.params.subscribe((data) => {
      this.subscriptionId = data.id
      this.subscription.get(this.subscriptionId).subscribe((subscription) => {
        if (subscription.isNewsletterAccepted) {
          const loadedSubscription = clone(subscription)
          loadedSubscription.isNewsletterAccepted = false
          this.subscription.update(loadedSubscription).subscribe(
            (updatedSubscription) => {
              this.confirmationSuccessful = true
            },
            (error) => {}
          )
        } else {
          this.confirmationSuccessful = true
        }
      })
    })
  }
}

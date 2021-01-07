import { AfterViewInit, Component } from '@angular/core'
import { ActivatedRoute, Router } from '@angular/router'

import { clone } from 'lodash-es'
import { SubscriptionService } from '@bendsoft/ticketing-api'

@Component({
  selector: 'app-newsletter-unsubscribe',
  templateUrl: './newsletter-unsubscribe.component.html',
  styleUrls: ['./newsletter-unsubscribe.component.scss']
})
export class NewsletterUnsubscribeComponent implements AfterViewInit {
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private subscriptionService: SubscriptionService
  ) {}

  animationTime = 4
  confirmationSuccessful: boolean
  progressLoading = false
  subscriptionId: string

  ngAfterViewInit(): void {
    this.route.params.subscribe((data) => {
      this.subscriptionId = data.id
      this.subscriptionService.get(this.subscriptionId).subscribe((subscription) => {
        if (subscription.isNewsletterAccepted) {
          const loadedSubscription = clone(subscription)
          loadedSubscription.isNewsletterAccepted = false
          this.subscriptionService.update(loadedSubscription).subscribe(
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

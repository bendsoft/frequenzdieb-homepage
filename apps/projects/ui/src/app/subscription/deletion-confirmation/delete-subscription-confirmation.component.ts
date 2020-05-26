import { AfterViewInit, Component } from '@angular/core'
import { ActivatedRoute, Router } from '@angular/router'
import { ApiService } from '../../service/common/api/api.service'

@Component({
  selector: 'app-delete-subscription-confirmation',
  templateUrl: './delete-subscription-confirmation.component.html',
  styleUrls: ['./delete-subscription-confirmation.component.scss']
})
export class DeleteSubscriptionConfirmationComponent implements AfterViewInit {
  animationTime = 4
  confirmationSuccessful: boolean
  progressLoading = false
  subscriptionId: string
  signature: string

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private api: ApiService
  ) {}

  ngAfterViewInit() {
    this.route.queryParams.subscribe((data) => {
      this.signature = data.signature
    })
    this.route.params.subscribe((data) => {
      this.subscriptionId = data.id
      this.api
        .deleteSubscription(this.subscriptionId, this.signature)
        .subscribe(
          (answer) => {
            this.confirmationSuccessful = true
            this.progressLoading = true
            setTimeout(() => {
              this.router.navigateByUrl('/home')
            }, this.animationTime * 1000)
          },
          (error) => {
            console.error(error)
            this.confirmationSuccessful = false
          }
        )
    })
  }
}

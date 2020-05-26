import { AfterViewInit, Component } from '@angular/core'
import { ActivatedRoute, Router } from '@angular/router'
import { ApiService } from '../../../service/common/api/api.service'

@Component({
  selector: 'app-newsletter-unsubscribe',
  templateUrl: './newsletter-unsubscribe.component.html',
  styleUrls: ['./newsletter-unsubscribe.component.scss']
})
export class NewsletterUnsubscribeComponent implements AfterViewInit {
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private api: ApiService
  ) {}

  animationTime = 4
  confirmationSuccessful: boolean
  progressLoading = false
  subscriptionId: string

  ngAfterViewInit(): void {
    this.route.params.subscribe((data) => {
      this.subscriptionId = data.id
      this.api.unsubscribeFromNewsletterById(this.subscriptionId).subscribe(
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

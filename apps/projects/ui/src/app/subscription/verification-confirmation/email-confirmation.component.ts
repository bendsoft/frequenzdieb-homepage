import { AfterViewInit, Component } from '@angular/core'
import { ActivatedRoute, Router } from '@angular/router'
import { SubscriptionService } from '@bendsoft/ticketing-api'

@Component({
  selector: 'app-email-confirmation',
  templateUrl: './email-confirmation.component.html',
  styleUrls: ['./email-confirmation.component.scss']
})
export class EmailConfirmationComponent implements AfterViewInit {
  animationTime = 4
  confirmationSuccessful: boolean
  progressLoading = false

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private api: SubscriptionService
  ) {}

  ngAfterViewInit() {
    this.route.params.subscribe((params) => {
      this.route.queryParams.subscribe((queryParams) => {
        this.api.confirmEmail(params.id, queryParams.signature).subscribe(
          () => {
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
    })
  }
}

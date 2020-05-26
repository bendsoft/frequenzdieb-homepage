import { NgModule } from '@angular/core'
import { RouterModule, Routes } from '@angular/router'

import { HomeComponent } from './home/home.component'
import { EmailConfirmationComponent } from './subscription/verification-confirmation/email-confirmation.component'
import { NewsletterUnsubscribeComponent } from './subscription/newsletter/unsubscribe/newsletter-unsubscribe.component'
import { DataProcessingAgreementComponent } from './footer/data-processing-agreement/data-processing-agreement.component'
import { ImpressumComponent } from './footer/impressum/impressum.component'
import { DeleteSubscriptionComponent } from './subscription/delete/delete-subscription.component'
import { DeleteSubscriptionConfirmationComponent } from './subscription/deletion-confirmation/delete-subscription-confirmation.component'

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'subscription/:id/confirm', component: EmailConfirmationComponent },
  {
    path: 'subscription/:id/newsletter/unsubscribe',
    component: NewsletterUnsubscribeComponent
  },
  { path: 'subscription/delete', component: DeleteSubscriptionComponent },
  { path: 'datenschutz', component: DataProcessingAgreementComponent },
  { path: 'impressum', component: ImpressumComponent },
  {
    path: 'subscription/:id/remove',
    component: DeleteSubscriptionConfirmationComponent
  },
  {
    path: 'newsletter/subscribe',
    component: HomeComponent,
    data: {
      showSubscriptionPopup: true
    }
  },
  {
    path: 'tickets/purchase',
    component: HomeComponent,
    data: {
      showTicketsPopup: true
    }
  },
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: '**', redirectTo: 'home', pathMatch: 'full' }
]

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {}

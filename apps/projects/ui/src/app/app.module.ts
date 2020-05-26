import { BrowserModule } from '@angular/platform-browser'
import { NgModule } from '@angular/core'
import { HttpClientModule } from '@angular/common/http'

import { FormsModule, ReactiveFormsModule } from '@angular/forms'
import { RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module } from 'ng-recaptcha'

import { AppRoutingModule } from './app-routing.module'
import { AppComponent } from './app.component'
import { HomeComponent } from './home/home.component'
import { HeaderComponent } from './header/header.component'
import { FooterComponent } from './footer/footer.component'
import { NewsletterSubComponent } from './subscription/newsletter/subscribe/newsletter-sub.component'
import { EmailConfirmationComponent } from './subscription/verification-confirmation/email-confirmation.component'
import { TicketOrderPopupComponent } from './ticketing/popup/ticket-order-popup.component'
import { NewsletterUnsubscribeComponent } from './subscription/newsletter/unsubscribe/newsletter-unsubscribe.component'
import { ImpressumComponent } from './footer/impressum/impressum.component'
import { DataProcessingAgreementComponent } from './footer/data-processing-agreement/data-processing-agreement.component'
import { DeleteSubscriptionComponent } from './subscription/delete/delete-subscription.component'
import { DeleteSubscriptionConfirmationComponent } from './subscription/deletion-confirmation/delete-subscription-confirmation.component'
import { MessageComponent } from './service/common/popup/message/message.component'

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    HeaderComponent,
    FooterComponent,
    NewsletterSubComponent,
    EmailConfirmationComponent,
    TicketOrderPopupComponent,
    NewsletterUnsubscribeComponent,
    ImpressumComponent,
    DataProcessingAgreementComponent,
    DeleteSubscriptionComponent,
    DeleteSubscriptionConfirmationComponent,
    MessageComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    RecaptchaV3Module
  ],
  providers: [
    {
      provide: RECAPTCHA_V3_SITE_KEY,
      useValue: '6LdTI_oUAAAAABff9icvv42UOsy8p0RW65V4o6Ks'
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}

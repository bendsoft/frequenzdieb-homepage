import { AfterViewInit, Component, OnInit } from '@angular/core'
import { FormBuilder, FormGroup } from '@angular/forms'
import { Router } from '@angular/router'
import { HttpResponse } from '@angular/common/http'
import {
  Concert,
  DatatransPayment,
  EventService,
  Subscription,
  SubscriptionService,
  TicketService
} from '@bendsoft/ticketing-api'
import { OpenPopupsService } from '../../common/popup/open-popups.service'

// Datatrans is somehow not recognized, bad temporary fix, hopefully :D
declare var Datatrans: any

@Component({
  selector: 'app-ticket-order-popup',
  templateUrl: './ticket-order-popup.component.html',
  styleUrls: ['./ticket-order-popup.component.scss']
})
export class TicketOrderPopupComponent implements OnInit, AfterViewInit {
  private readonly directNewsletterSubscriptionUrl = '/tickets/purchase'
  private readonly popupName = 'tickets'

  ticketForm: FormGroup

  ticketFormStages: string[] = [
    'password',
    'basket',
    'contactInfo',
    'emailVerification',
    'payment',
    'paymentComplete'
  ]

  ticketFormStage: string = this.ticketFormStages[0]

  concert: Concert

  totalPrice = 0

  // for testing
  ticket

  ticketPrice = 1850
  ticketId = '5ea5e952737e051f1760855f'
  currency = 'CHF'
  ticketSignature: string
  merchantId: string

  userId: string

  isOpen: boolean

  confirmedTicketUrl: string

  constructor(
    private ticketService: TicketService,
    private eventService: EventService,
    private subscriptionService: SubscriptionService,
    public popups: OpenPopupsService,
    private route: Router,
    private formBuilder: FormBuilder
  ) {
    this.ticketForm = this.formBuilder.group({
      password: '',
      amount: 0,
      name: '',
      surname: '',
      email: '',
      newsletterAccepted: false
    })
  }

  ngOnInit() {
    this.popups.addPopup(this.popupName, this)
    this.eventService.getConcert('5e94cb5a63f0ff677e5a2691').subscribe((data: Concert) => {
      this.concert = data
    })
  }

  ngAfterViewInit() {
    // for easier development
    this.ticketFormStage = 'payment'
    this.popups.setPopupOpen('tickets', true)
  }

  ngOnDestroy() {
    this.popups.removePopup(this.popupName)
  }

  startPayment() {
    this.ticketService
      .createPayment(this.ticketId, {
        amount: this.ticketPrice,
        currency: this.currency
      })
      .subscribe((paymentInfo: DatatransPayment) => {
        console.log(paymentInfo)
        Datatrans.startPayment({
          // form: '#paymentForm',
          params: {
            merchantId: paymentInfo.merchantId,
            amount: paymentInfo.amount,
            currency: paymentInfo.currency,
            refno: paymentInfo.reference,
            sign: paymentInfo.signature
          },
          opened: (e) => console.log('opened', e),
          loaded: (e) => console.log('loaded', e),
          closed: (e) => console.log('closed', e),
          // params: {
          //   merchantId: paymentInfo.merchantId,
          //   amount: paymentInfo.amount,
          //   currency: paymentInfo.currency,
          //   refno: paymentInfo.reference,
          //   sign: paymentInfo.signature,
          //   successUrl: 'https://docs.datatrans.ch/docs/integrations-payment-page-lightbox-mode',
          //   errorUrl: 'https://docs.datatrans.ch/docs/integrations-payment-page-lightbox-mode',
          //   cancelUrl: 'https://docs.datatrans.ch/docs/integrations-payment-page-lightbox-mode',
          //   uppWebResponseMethod: 'GET'
          // },
          error: (error) => console.error(error)
        })
      })
  }

  closePopup() {
    this.popups.setPopupOpen('tickets', false)
    if (this.isOpen === false && this.route.url === this.directNewsletterSubscriptionUrl) {
      this.route.navigateByUrl('/home')
    }
    // this.ticketForm.reset()
  }

  checkPassword() {
    // this.api.checkPassword(this.ticketForm.value.password).subscribe((data) => {
    //   console.log(data)
    //   this.ticketFormStage = 2
    // })
    this.ticketFormStage = 'basket'
  }

  addTicket() {
    this.ticketForm.patchValue({
      amount: this.ticketForm.value.amount + 1
    })
    this.calculateTotal()
  }

  removeTicket() {
    if (this.ticketForm.value.amount > 0) {
      this.ticketForm.patchValue({
        amount: this.ticketForm.value.amount - 1
      })
    }
    this.calculateTotal()
  }

  calculateTotal() {
    this.totalPrice = this.ticketForm.value.amount * this.ticketPrice
  }

  selectTickets() {
    if (this.ticketForm.value.amount !== 0) {
      this.ticketFormStage = 'contactInfo'
    }
  }

  checkEmailConfirmation() {
    this.subscriptionService
      .getByEmail(this.ticketForm.value.email)
      .subscribe((subscribedUser: Subscription) => {
        if (subscribedUser.isConfirmed) {
          this.ticketFormStage = 'payment'
        }
      })
  }

  createNewSubscription() {
    this.subscriptionService
      .create({
        email: this.ticketForm.value.email,
        name: this.ticketForm.value.name,
        surname: this.ticketForm.value.surname,
        isNewsletterAccepted: this.ticketForm.value.newsletterAccepted
      })
      .subscribe((res: Subscription) => {
        console.log(res)
        this.userId = res.id
      })
  }

  resendConfirmationMail(userId) {
    return this.subscriptionService.requestEmailConfirmation(userId)
  }

  checkContactInfo() {
    this.subscriptionService.getByEmail(this.ticketForm.value.email).subscribe(
      (subscribedUser: Subscription) => {
        if (subscribedUser.id && !subscribedUser.isConfirmed) {
          this.resendConfirmationMail(subscribedUser.id).subscribe(
            (response: HttpResponse<void>) => {
              console.log(response)
            }
          )
          this.ticketFormStage = 'emailVerification'
          // this.updateContactInfo(subscribedUser)
        } else if (subscribedUser.id && subscribedUser.isConfirmed) {
          this.ticketFormStage = 'payment'
        }
      },
      (error) => {
        console.log(error)
        if (error.status === 404) {
          this.createNewSubscription()
          this.ticketFormStage = 'emailVerification'
        }
      }
    )
  }

  updateContactInfo(user) {
    // set name and address of subscription to data entered in form
    return true
  }

  proceedtoPayment() {
    // tbd: opens payment interface
    this.ticketFormStage = 'payment'
  }

  paymentResponse() {
    this.ticketFormStage = 'paymentComplete'
  }

  setTicketFormStage(stage: string) {
    this.ticketFormStage = stage
  }
}

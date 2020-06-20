import { Component, OnInit } from '@angular/core'

import { ActivatedRoute } from '@angular/router'

import { Concert, EventService } from '@bendsoft/ticketing-api'
import { OpenPopupsService } from '../common/popup/open-popups.service'

declare let $: any

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  concert

  mouse = {
    x: 0,
    y: 0
  }

  constructor(
    private eventService: EventService,
    private route: ActivatedRoute,
    private popups: OpenPopupsService
  ) {}

  ngOnInit() {
    this.route.data.subscribe((data) => {
      this.popups.setPopupOpen('newsletter', data.showSubscriptionPopup)
      this.popups.setPopupOpen('tickets', data.showTicketsPopup)
    })
    console.log('hi')
    this.eventService
      .getConcert('5e94cb5a63f0ff677e5a2691')
      .subscribe((concert: Concert) => {
        this.concert = concert
      })

    $(window).on('mousemove', (e) => {
      this.mouse.x = e.pageX / window.innerWidth - 0.5
      this.mouse.y = e.pageY / window.innerHeight - 0.5
      $('.date').css(
        'transform',
        `translate(${-50 + this.mouse.x * 20}%, ${
          -50 + this.mouse.y * 20
        }%) rotateY(${this.mouse.x * 30}deg)`
      )
      $('.date .shadow').css(
        'transform',
        `translate(-50%, ${-this.mouse.y * 130}%) scale(${
          this.mouse.y + 1.2
        }) rotateZ(${this.mouse.x * 10}deg)`
      )
      $('.next-concert').css(
        'transform',
        `translate(${-50 + -this.mouse.x * 20}%, ${
          -50 + -this.mouse.y * 20
        }%) rotateY(${-this.mouse.x * 30}deg) rotateZ(-13deg) rotateX(${
          this.mouse.y * 20
        }deg)`
      )
      $('.date .location').css(
        'letter-spacing',
        `${Math.abs(this.mouse.x) * 5}px`
      )
    })
  }
}

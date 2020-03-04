import { Component, OnInit } from '@angular/core';

declare var $:any;

@Component({
  selector: 'app-newsletter-sub',
  templateUrl: './newsletter-sub.component.html',
  styleUrls: ['./newsletter-sub.component.scss']
})
export class NewsletterSubComponent implements OnInit {

  closeNewsletterSub () {
    $(".newsletter-sub-window, .backdrop").removeClass("open");
  }

  submitNewsletterSub () {

  }

  constructor() { }

  ngOnInit() {
  }

}

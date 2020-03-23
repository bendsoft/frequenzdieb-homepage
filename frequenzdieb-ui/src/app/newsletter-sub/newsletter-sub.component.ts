import { Component, OnInit } from '@angular/core';

import { ApiService } from './../api.service';

declare var $: any;

@Component({
  selector: 'app-newsletter-sub',
  templateUrl: './newsletter-sub.component.html',
  styleUrls: ['./newsletter-sub.component.scss']
})
export class NewsletterSubComponent implements OnInit {

  closeNewsletterSub() {
    $(".newsletter-sub-window, .backdrop").removeClass("open");
  }

  submitNewsletterSub() {
    var mail = $(".newsletter-sub-window input").val();
    console.log(mail);
    this.api.getSubscription(mail).subscribe(data => {
      console.log(data);
    });
  }

  constructor(
    private api: ApiService
  ) {
  }

  ngOnInit() {

  }

}

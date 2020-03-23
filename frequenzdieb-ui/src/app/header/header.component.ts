import { Component, OnInit } from '@angular/core';

declare var $: any;

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  openNewsletterWindow() {
    $(".newsletter-sub-window, .backdrop").addClass("open");
  }

  constructor() {
  }

  ngOnInit() {
  }

}

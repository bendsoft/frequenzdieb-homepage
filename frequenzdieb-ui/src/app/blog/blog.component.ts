import { Component, OnInit } from '@angular/core';
import { ApiService } from "./../api.service";

@Component({
  selector: 'app-blog',
  templateUrl: './blog.component.html',
  styleUrls: ['./blog.component.scss']
})

export class BlogComponent implements OnInit {

  blogEntries;

  constructor(
    private api: ApiService
  ) {
  }

  ngOnInit() {
    this.api.getBlogEntries().subscribe((data) => {
      console.log(data);
      this.blogEntries = data;
    });
  }

}

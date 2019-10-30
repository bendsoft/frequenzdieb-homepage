import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { HomeComponent } from "./home/home.component";
import { AboutComponent } from "./about/about.component";
import { BlogComponent } from "./blog/blog.component";
import { GalleryComponent } from "./gallery/gallery.component";
import { ContactComponent } from "./contact/contact.component";
import { TicketsComponent } from "./tickets/tickets.component";


const routes: Routes = [
  { path: "home", component: HomeComponent},
  { path: "band", component: AboutComponent},
  { path: "blog", component: BlogComponent},
  { path: "gallerie", component: GalleryComponent},
  { path: "kontakt", component: ContactComponent},
  { path: "tickets", component: TicketsComponent},
  { path: "", redirectTo: "home", pathMatch: "full"},
  { path: "**", redirectTo: "home", pathMatch: "full"}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

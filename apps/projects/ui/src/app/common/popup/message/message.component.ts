import { Component, OnInit } from '@angular/core'
import { OpenPopupsService } from '../open-popups.service'

@Component({
  selector: 'app-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.scss']
})
export class MessageComponent implements OnInit {
  private readonly popupName = 'message'

  isOpen
  data
  constructor(public popups: OpenPopupsService) {}

  ngOnInit(): void {
    this.popups.addPopup(this.popupName, this)
  }

  ngOnDestroy() {
    this.popups.removePopup(this.popupName)
  }
}

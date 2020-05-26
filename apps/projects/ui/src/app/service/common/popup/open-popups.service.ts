import { Injectable } from '@angular/core'
import { Popup } from './open-popups.interface'

@Injectable({
  providedIn: 'root'
})
export class OpenPopupsService {
  private registeredPopups = new Map()

  addPopup(name: string, instance: Popup) {
    this.registeredPopups.set(name, instance)
  }

  removePopup(name: string) {
    this.registeredPopups.delete(name)
  }

  getAllPopups() {
    return this.registeredPopups
  }

  setPopupOpen(name: string, isOpen: boolean) {
    this.registeredPopups.get(name).isOpen = isOpen
  }

  setPopupData(name: string, data: any) {
    this.registeredPopups.get(name).data = data
  }

  isPopupOpen(name: string) {
    return this.registeredPopups.get(name).isOpen
  }
}

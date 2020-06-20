import { OnDestroy } from '@angular/core'

export interface Popup extends OnDestroy {
  isOpen: boolean
  data?: any
}

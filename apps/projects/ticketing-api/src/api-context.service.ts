import { Inject, Injectable, InjectionToken } from '@angular/core'
import { BehaviorSubject } from 'rxjs'
import { HttpHeaders } from '@angular/common/http'

export const BROWSER_STORAGE = new InjectionToken<Storage>('Browser Storage', {
  providedIn: 'root',
  factory: () => localStorage
})

@Injectable({
  providedIn: 'root'
})
export class ApiContextService {
  // apiServerUrl = 'https://dev-api.frequenzdieb.ch'
  apiServerUrl = 'http://localhost:8085/api'

  isAuthenticated = new BehaviorSubject(false)

  constructor(@Inject(BROWSER_STORAGE) public db: Storage) {}

  login(token) {
    this.db.setItem('jwt', token)
    this.isAuthenticated.next(true)
  }

  logout() {
    this.db.removeItem('jwt')
    this.isAuthenticated.next(false)
  }

  createWithAuthorizationHeaders(): Partial<{ headers?: HttpHeaders }> {
    return {
      headers: new HttpHeaders(this.createBearerHeader())
    }
  }

  createBearerHeader() {
    return {
      'Content-Type': 'application/json; charset=utf-8',
      Authorization: `Bearer ${this.db.getItem('jwt')}`
    }
  }
}

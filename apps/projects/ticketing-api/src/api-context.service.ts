import { Inject, Injectable, InjectionToken } from '@angular/core'
import { BehaviorSubject, Observable, throwError } from 'rxjs'
import { HttpErrorResponse, HttpHeaders } from '@angular/common/http'
import { catchError, switchMap } from 'rxjs/operators'
import { merge } from 'lodash'
import { ReCaptchaV3Service } from 'ng-recaptcha'
import { LocalizedErrorMessage } from './common/LocalizedErrorMessage'

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

  translateServerError<T>() {
    return catchError.bind((response: HttpErrorResponse) =>
      throwError(
        this.localizedErrorMessage.getErrorMessageFromResponse(response)
      )
    )
  }

  constructor(
    @Inject(BROWSER_STORAGE) public db: Storage,
    private recaptcha: ReCaptchaV3Service,
    private localizedErrorMessage: LocalizedErrorMessage
  ) {}

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

  enrichApiRequestWithRecaptcha<
    T extends Partial<{
      params: Record<string, string | string[]>
    }>,
    R
  >(
    actionName: string,
    httpRequestCallback: (
      httpOptions: T & { params: { recaptcha: string } }
    ) => Observable<R>,
    options?: T
  ): Observable<R> {
    return this.recaptcha.execute(actionName).pipe(
      // eslint-disable-next-line @typescript-eslint/ban-ts-ignore
      // @ts-ignore
      switchMap((token: string) =>
        httpRequestCallback(
          merge({}, options, {
            params: { recaptcha: token }
          })
        )
      )
    )
  }
}

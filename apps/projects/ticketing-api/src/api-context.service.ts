import { Inject, Injectable, InjectionToken, Optional } from '@angular/core'
import { BehaviorSubject, Observable, throwError } from 'rxjs'
import { HttpErrorResponse, HttpHeaders } from '@angular/common/http'
import { catchError, switchMap } from 'rxjs/operators'
import { isEmpty, merge } from 'lodash-es'
import { ReCaptchaV3Service } from 'ng-recaptcha'
import { ErrorMessageHandler } from './common/error-message-handler.service'

export const BROWSER_STORAGE = new InjectionToken<Storage>('Browser Storage', {
  providedIn: 'root',
  factory: () => localStorage
})

export const API_SERVER_URLS = new InjectionToken<string[]>('Api Server Url')

@Injectable({
  providedIn: 'root'
})
export class ApiContextService {
  static errorMessageHandlerInstance

  isAuthenticated = new BehaviorSubject(false)

  constructor(
    @Inject(BROWSER_STORAGE) public db: Storage,
    @Inject(API_SERVER_URLS) public apiServerHosts: string[],
    @Inject(ErrorMessageHandler) messageHandler,
    @Optional() private recaptcha: ReCaptchaV3Service
  ) {
    ApiContextService.errorMessageHandlerInstance = messageHandler

    if (isEmpty(this.getApiServer())) {
      this.setApiServer(this.apiServerHosts[0])
    }
  }

  setApiServer(url: string) {
    this.db.setItem('apiServerHost', url)
  }

  getApiServer() {
    return this.db.getItem('apiServerHost')
  }

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
      httpOptions: (T & { params: { recaptcha: string } }) | T
    ) => Observable<R>,
    options?: T
  ): Observable<R> {
    if (this.recaptcha !== null) {
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

    return httpRequestCallback(options)
  }
}

export function catchServerError(translator = ApiContextService.errorMessageHandlerInstance) {
  return catchError((response: HttpErrorResponse) =>
    throwError(translator.getErrorMessageFromResponse(response))
  )
}

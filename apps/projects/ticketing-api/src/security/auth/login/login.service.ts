import { Inject, Injectable } from '@angular/core'
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http'
import { catchError, tap } from 'rxjs/operators'
import { throwError } from 'rxjs'
import { LoginRequest } from './LoginRequest'
import { LoginResponse } from './LoginResponse'
import { LocalizedErrorMessage } from '../../../common/LocalizedErrorMessage'
import { ApiContextService } from '../../../api-context.service'

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private readonly loginApiUrl

  constructor(
    private httpClient: HttpClient,
    private apiContext: ApiContextService,
    @Inject(LocalizedErrorMessage)
    private localizedErrorMessage: LocalizedErrorMessage
  ) {
    this.loginApiUrl = `${apiContext.apiServerUrl}/security/auth/login`
  }

  login(loginRequest: LoginRequest, params?: HttpParams) {
    return this.httpClient
      .post(this.loginApiUrl, loginRequest, { params })
      .pipe(
        tap(
          (response: LoginResponse) => {
            this.apiContext.login(response.token)
          },
          () => this.apiContext.logout()
        ),
        catchError((response: HttpErrorResponse) =>
          throwError(
            this.localizedErrorMessage.getErrorMessageFromResponse(response)
          )
        )
      )
  }
}

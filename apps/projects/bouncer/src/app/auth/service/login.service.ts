import { Injectable } from '@angular/core'
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http'
import { catchError, tap } from 'rxjs/operators'
import { throwError } from 'rxjs'
import { ApplicationContextService } from '../../common/service/application-context.service'
import { LoginRequest } from './LoginRequest'
import { LoginResponse } from './LoginResponse'
import { LocalizedErrorMessage } from '../../common/LocalizedErrorMessage'

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private readonly loginApiUrl

  constructor(
    private httpClient: HttpClient,
    private applicationContext: ApplicationContextService
  ) {
    this.loginApiUrl = `${applicationContext.apiServerUrl}/security/auth/login`
  }

  login(loginRequest: LoginRequest, params?: HttpParams) {
    return this.httpClient
      .post(this.loginApiUrl, loginRequest, { params })
      .pipe(
        tap(
          (response: LoginResponse) => {
            this.applicationContext.login(response.token)
          },
          () => this.applicationContext.logout()
        ),
        catchError((response: HttpErrorResponse) =>
          throwError(
            LocalizedErrorMessage.getErrorMessageFromResponse(response)
          )
        )
      )
  }
}

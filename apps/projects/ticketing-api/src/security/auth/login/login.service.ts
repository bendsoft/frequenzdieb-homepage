import { Injectable } from '@angular/core'
import { HttpClient, HttpParams } from '@angular/common/http'
import { tap } from 'rxjs/operators'
import { LoginRequest } from '../../../@types/loginRequest'
import { LoginResponse } from '../../../@types/loginResponse'
import { catchServerError } from '../../../common/error-message-handler.service'
import { ApiContextService } from '../../../api-context.service'

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private readonly loginApiUrl

  constructor(private httpClient: HttpClient, private apiContext: ApiContextService) {
    this.loginApiUrl = `${apiContext.apiServerUrl}/security/auth/login`
  }

  login(loginRequest: LoginRequest, params?: HttpParams) {
    return this.httpClient.post(this.loginApiUrl, loginRequest, { params }).pipe(
      tap(
        (response: LoginResponse) => {
          this.apiContext.login(response.token)
        },
        () => this.apiContext.logout()
      ),
      catchServerError()
    )
  }
}

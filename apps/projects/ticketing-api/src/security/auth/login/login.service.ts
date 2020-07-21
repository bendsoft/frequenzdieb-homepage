import { Injectable } from '@angular/core'
import { HttpClient, HttpParams } from '@angular/common/http'
import { tap } from 'rxjs/operators'
import { LoginRequest } from '../../../@types/loginRequest'
import { LoginResponse } from '../../../@types/loginResponse'
import { ApiContextService, catchServerError } from '../../../api-context.service'

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private get loginRoute() {
    return `${this.apiContext.getApiServer()}/security/auth/login`
  }

  constructor(private httpClient: HttpClient, private apiContext: ApiContextService) {}

  login(loginRequest: LoginRequest, params?: HttpParams) {
    return this.httpClient.post(this.loginRoute, loginRequest, { params }).pipe(
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

import { HttpErrorResponse } from '@angular/common/http'
import { cloneDeep } from 'lodash'
import { Injectable } from '@angular/core'

@Injectable({
  providedIn: 'root'
})
export class LocalizedErrorMessage {
  public INVALID_REQUEST = 'Something went wrong with the Request.'
  public UNKNOWN_ERROR = 'An unknown Error has happened.'
  public SERVER_ERROR = 'An Error happened on the server-side.'
  public NOT_FOUND = 'Not found.'
  public NOT_AUTHORIZED = 'Not Authorized or Access-Token missing or invalid.'

  valueOf(key: string) {
    return this[key] || ''
  }

  getErrorMessageFromResponse(response: HttpErrorResponse) {
    const localizedResponse = cloneDeep(response)

    switch (response.status) {
      case 404:
        localizedResponse.message = this.NOT_FOUND
        break
      case 401:
        localizedResponse.message = this.NOT_AUTHORIZED
        break
      case 403:
        localizedResponse.message = this.INVALID_REQUEST
        break
      default: {
        if (LocalizedErrorMessage.hasServerError(response)) {
          localizedResponse.message = this.SERVER_ERROR
        }
        const errorMessage = this.valueOf(response.message)
        if (errorMessage) {
          localizedResponse.message = errorMessage
        }
        localizedResponse.message = this.UNKNOWN_ERROR
      }
    }

    return localizedResponse
  }

  private static hasServerError(response: HttpErrorResponse) {
    return response.status / 100 >= 5
  }
}

import { HttpErrorResponse } from '@angular/common/http'
import { cloneDeep } from 'lodash'

export abstract class LocalizedErrorMessage {
  public static INVALID_REQUEST = $localize`:@@InvalidRequestErrorMsg:Something went wrong with the Request.`
  public static UNKNOWN_ERROR = $localize`:@@UnknownErrorErrorMsg:An unknown Error has happened.`
  public static SERVER_ERROR = $localize`:@@ServerErrorErrorMsg:An Error happened on the server-side.`
  public static NOT_FOUND = $localize`:@@NotFoundErrorMsg:Not found.`
  public static NOT_AUTHORIZED = $localize`:@@NotAuthorizedErrorMsg:Not Authorized or Access-Token missing or invalid.`

  static valueOf(key: string) {
    return this[key] || ''
  }

  static getErrorMessageFromResponse(response: HttpErrorResponse) {
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
        if (this.hasServerError(response)) {
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

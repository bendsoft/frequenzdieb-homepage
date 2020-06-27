import { ErrorStateMatcher } from '@angular/material/core'
import {
  FormControl,
  FormGroupDirective,
  NgForm,
  ValidationErrors,
  ValidatorFn
} from '@angular/forms'

export class ApiErrorStateMatcher implements ErrorStateMatcher {
  errorMessage

  apiErrorValidator(): ValidatorFn {
    return () => this.hasApiError()
  }

  hasApiError(): ValidationErrors | null {
    return this.errorMessage ? { apiError: true } : null
  }

  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const isSubmitted = form && form.submitted
    const isControlInvalid = control && control.invalid
    return !!(isControlInvalid && isSubmitted)
  }
}

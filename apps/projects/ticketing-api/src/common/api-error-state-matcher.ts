import { ErrorStateMatcher } from '@angular/material/core'
import {
  AbstractControl,
  FormControl,
  FormGroupDirective,
  NgForm,
  ValidationErrors,
  ValidatorFn
} from '@angular/forms'

export class ApiErrorStateMatcher implements ErrorStateMatcher {
  errorMessage
  private validateControls: Set<AbstractControl>

  constructor(setValidator: boolean, ...controls: AbstractControl[]) {
    this.validateControls = new Set(controls)
    this.validateControls.forEach((control) => control.setValidators(this.apiErrorValidator()))
  }

  apiErrorValidator(): ValidatorFn {
    return () => this.hasApiError()
  }

  update(errorMessage: string) {
    this.errorMessage = errorMessage
    this.validateControls.forEach((control) =>
      control.updateValueAndValidity({
        onlySelf: true
      })
    )
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

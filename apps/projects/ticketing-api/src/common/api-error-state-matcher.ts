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

  constructor(
    private afterSubmit = true,
    private control: AbstractControl,
    ...validators: ValidatorFn[]
  ) {
    validators.push(this.apiErrorValidator())
    control.setValidators(validators)
    this.update()
  }

  apiErrorValidator(): ValidatorFn {
    return () => this.hasApiError()
  }

  update(errorMessage?: string) {
    this.errorMessage = errorMessage
    this.control.updateValueAndValidity({
      onlySelf: true
    })
  }

  hasApiError(): ValidationErrors | null {
    return this.errorMessage ? { apiError: true } : null
  }

  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const isSubmitted = form && form.submitted
    const isControlInvalid = control && control.invalid
    return !!(isControlInvalid && (this.afterSubmit === false ? control.touched : isSubmitted))
  }
}

import { Component, OnInit } from '@angular/core'
import { FormBuilder, FormGroup } from '@angular/forms'
import { ApiService } from '../../service/common/api/api.service'

@Component({
  selector: 'app-delete-subscription',
  templateUrl: './delete-subscription.component.html',
  styleUrls: ['./delete-subscription.component.scss']
})
export class DeleteSubscriptionComponent implements OnInit {
  deleteAccountForm: FormGroup
  constructor(private formBuilder: FormBuilder, private api: ApiService) {
    this.deleteAccountForm = this.formBuilder.group({
      email: ''
    })
  }

  submitted = false

  ngOnInit(): void {}

  deleteAccount() {
    console.log(this.deleteAccountForm.value.email)
    this.api
      .requestSubscriptionDeletion(this.deleteAccountForm.value.email)
      .subscribe((res) => {
        console.log(res)
      })
    this.deleteAccountForm.reset()
    this.submitted = true
  }
}

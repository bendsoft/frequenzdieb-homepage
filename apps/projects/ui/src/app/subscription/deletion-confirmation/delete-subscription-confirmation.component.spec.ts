import { async, ComponentFixture, TestBed } from '@angular/core/testing'

import { DeleteSubscriptionConfirmationComponent } from './delete-subscription-confirmation.component'

describe('DeleteSubscriptionConfirmationComponent', () => {
  let component: DeleteSubscriptionConfirmationComponent
  let fixture: ComponentFixture<DeleteSubscriptionConfirmationComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DeleteSubscriptionConfirmationComponent]
    }).compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteSubscriptionConfirmationComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})

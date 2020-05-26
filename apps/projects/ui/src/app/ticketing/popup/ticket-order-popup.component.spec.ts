import { async, ComponentFixture, TestBed } from '@angular/core/testing'

import { TicketOrderPopupComponent } from './ticket-order-popup.component'

describe('TicketOrderPopupComponent', () => {
  let component: TicketOrderPopupComponent
  let fixture: ComponentFixture<TicketOrderPopupComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TicketOrderPopupComponent]
    }).compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(TicketOrderPopupComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})

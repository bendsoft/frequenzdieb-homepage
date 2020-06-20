import { async, ComponentFixture, TestBed } from '@angular/core/testing'

import { TicketScannerPopupComponent } from './ticket-scanner-popup.component'

describe('TicketScannerPopupComponent', () => {
  let component: TicketScannerPopupComponent
  let fixture: ComponentFixture<TicketScannerPopupComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TicketScannerPopupComponent]
    }).compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(TicketScannerPopupComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})

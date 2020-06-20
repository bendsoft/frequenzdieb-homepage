import { async, ComponentFixture, TestBed } from '@angular/core/testing'

import { TicketScannerComponent } from './ticket-scanner.component'

describe('BncrTicketScannerComponent', () => {
  let component: TicketScannerComponent
  let fixture: ComponentFixture<TicketScannerComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TicketScannerComponent]
    }).compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(TicketScannerComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})

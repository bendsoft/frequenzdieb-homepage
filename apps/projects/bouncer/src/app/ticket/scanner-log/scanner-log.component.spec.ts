import { async, ComponentFixture, TestBed } from '@angular/core/testing'

import { ScannerLogComponent } from './scanner-log.component'

describe('ScannerLogComponent', () => {
  let component: ScannerLogComponent
  let fixture: ComponentFixture<ScannerLogComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ScannerLogComponent]
    }).compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(ScannerLogComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})

import { async, ComponentFixture, TestBed } from '@angular/core/testing'

import { NewsletterSubComponent } from './newsletter-sub.component'

describe('NewsletterSubComponent', () => {
  let component: NewsletterSubComponent
  let fixture: ComponentFixture<NewsletterSubComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NewsletterSubComponent]
    }).compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(NewsletterSubComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})

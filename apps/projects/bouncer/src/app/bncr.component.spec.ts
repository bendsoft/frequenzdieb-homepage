import { async, TestBed } from '@angular/core/testing'
import { RouterTestingModule } from '@angular/router/testing'
import { BncrComponent } from './bncr.component'

describe('AppComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [BncrComponent]
    }).compileComponents()
  }))

  it('should create the app', () => {
    const fixture = TestBed.createComponent(BncrComponent)
    const app = fixture.componentInstance
    expect(app).toBeTruthy()
  })

  it("should have as title 'frequenzdieb-bouncer'", () => {
    const fixture = TestBed.createComponent(BncrComponent)
    const app = fixture.componentInstance
    expect(app.title).toEqual('frequenzdieb-bouncer')
  })

  it('should render title', () => {
    const fixture = TestBed.createComponent(BncrComponent)
    fixture.detectChanges()
    const compiled = fixture.nativeElement
    expect(compiled.querySelector('.content span').textContent).toContain(
      'frequenzdieb-bouncer app is running!'
    )
  })
})

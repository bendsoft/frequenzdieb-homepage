import { TestBed } from '@angular/core/testing'

import { ApplicationContextService } from './application-context.service'

describe('LoginStateServiceService', () => {
  let service: ApplicationContextService

  beforeEach(() => {
    TestBed.configureTestingModule({})
    service = TestBed.inject(ApplicationContextService)
  })

  it('should be created', () => {
    expect(service).toBeTruthy()
  })
})

import { TestBed } from '@angular/core/testing'

import { ApiContextService } from './api-context.service'

describe('LoginStateServiceService', () => {
  let service: ApiContextService

  beforeEach(() => {
    TestBed.configureTestingModule({})
    service = TestBed.inject(ApiContextService)
  })

  it('should be created', () => {
    expect(service).toBeTruthy()
  })
})

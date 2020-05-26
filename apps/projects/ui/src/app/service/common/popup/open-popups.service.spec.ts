import { TestBed } from '@angular/core/testing'

import { OpenPopupsService } from './open-popups.service'

describe('OpenPopupsService', () => {
  let service: OpenPopupsService

  beforeEach(() => {
    TestBed.configureTestingModule({})
    service = TestBed.inject(OpenPopupsService)
  })

  it('should be created', () => {
    expect(service).toBeTruthy()
  })
})

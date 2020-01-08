import { TestBed } from '@angular/core/testing';

import { UserInfoService } from './user-info.service';

describe('UserInfoService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: UserInfoService = TestBed.get(UserInfoService);
    expect(service).toBeTruthy();
  });
});

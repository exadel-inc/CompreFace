import { TestBed } from '@angular/core/testing';

import { AppUserService } from './app-user.service';

describe('AppUserService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AppUserService = TestBed.get(AppUserService);
    expect(service).toBeTruthy();
  });
});

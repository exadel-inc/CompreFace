import { TestBed } from '@angular/core/testing';

import { DemoGuard } from './demo.guard';

describe('DemoGuard', () => {
  let guard: DemoGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    guard = TestBed.inject(DemoGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});

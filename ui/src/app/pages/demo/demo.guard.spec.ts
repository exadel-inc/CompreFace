import { TestBed, waitForAsync } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { provideMockStore } from '@ngrx/store/testing';
import { of } from 'rxjs';

import { DemoGuard } from './demo.guard';
import { DemoService } from './demo.service';

describe('DemoGuard', () => {
  let guard: DemoGuard;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        providers: [
          provideMockStore(),
          {
            provide: Store,
            useValue: { dispatch: () => {} },
          },
          {
            provide: Router,
            useValue: { navigate: () => {} },
          },
          {
            provide: DemoService,
            useValue: { getModel: () => of({}) },
          },
        ],
      }).compileComponents();
      guard = TestBed.inject(DemoGuard);
    })
  );

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});

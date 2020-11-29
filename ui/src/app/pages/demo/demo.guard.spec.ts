import { TestBed } from '@angular/core/testing';

import { DemoGuard } from './demo.guard';
import { provideMockStore } from '@ngrx/store/testing';
import { DemoService } from './demo.service';
import { of } from 'rxjs';
import { Store } from '@ngrx/store';
import { Router } from '@angular/router';

describe('DemoGuard', () => {
  let guard: DemoGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        provideMockStore()
      ],
      providers: [
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
        }
      ]
    });
    guard = TestBed.inject(DemoGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});

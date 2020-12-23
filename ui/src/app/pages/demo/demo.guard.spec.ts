import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { provideMockStore } from '@ngrx/store/testing';
import { of } from 'rxjs';

import { DemoGuard } from './demo.guard';
import { DemoService } from './demo.service';

xdescribe('DemoGuard', () => {
  let guard: DemoGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [provideMockStore()],
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
        },
      ],
    });
    guard = TestBed.inject(DemoGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});

import { TestModelComponent } from './test-model.component';
import { TestModelPageService } from './test-model.service';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('TestModelComponent', () => {
  let component: TestModelComponent;
  let fixture: ComponentFixture<TestModelComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [TestModelComponent],
        providers: [
          {
            provide: Store,
            useValue: {
              select: () => {},
            },
          },
          {
            provide: TestModelPageService,
            useValue: {
              initUrlBindingStreams: () => {},
              unSubscribe: () => {},
              clearSelectedModelId: () => {},
            },
          },
        ],
        schemas: [NO_ERRORS_SCHEMA],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(TestModelComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });
});

import {TestModelComponent} from './test-model.component';
import {TestModelPageService} from "./test-model.service";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {Store} from "@ngrx/store";

describe('TestModelComponent', () => {
  let component: TestModelComponent;
  let fixture: ComponentFixture<TestModelComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [TestModelComponent],
      providers: [{
        provide: TestModelPageService, useValue: {
          initUrlBindingStreams: () => {
          },
        }
      },
        {
          provide: Store, useValue: {
            select: () => {
            },
          }
        }],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestModelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });
});

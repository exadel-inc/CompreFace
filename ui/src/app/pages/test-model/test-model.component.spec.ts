import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestModelComponent } from './test-model.component';

describe('TestModelComponent', () => {
  let component: TestModelComponent;
  let fixture: ComponentFixture<TestModelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestModelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestModelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

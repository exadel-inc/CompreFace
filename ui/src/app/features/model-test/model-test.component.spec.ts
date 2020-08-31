import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelTestComponent } from './model-test.component';

describe('ModelTestComponent', () => {
  let component: ModelTestComponent;
  let fixture: ComponentFixture<ModelTestComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModelTestComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelTestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { alertComponent } from './alert.component';

describe('alertComponent', () => {
  let component: alertComponent;
  let fixture: ComponentFixture<alertComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ alertComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(alertComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

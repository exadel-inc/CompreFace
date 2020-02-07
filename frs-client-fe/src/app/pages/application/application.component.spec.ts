import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ApplicationComponent} from './application.component';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ApplicationPageService} from './application.service';

describe('ApplicationComponent', () => {
  let component: ApplicationComponent;
  let fixture: ComponentFixture<ApplicationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationComponent ],
      providers: [
        {provide: ApplicationPageService, useValue: {
            initUrlBindingStreams: () => {},
            unSubscribe: () => {},
          }}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

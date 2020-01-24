import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationUserListComponent } from './application-user-list.component';

describe('ApplicationUserListComponent', () => {
  let component: ApplicationUserListComponent;
  let fixture: ComponentFixture<ApplicationUserListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationUserListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationUserListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

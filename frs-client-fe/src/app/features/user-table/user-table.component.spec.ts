import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatTableModule, MatSelectModule} from '@angular/material';
import {UserTableComponent} from './user-table.component';
import {NO_ERRORS_SCHEMA} from '@angular/core';

describe('UserTableComponent', () => {
  let component: UserTableComponent;
  let fixture: ComponentFixture<UserTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MatTableModule, MatSelectModule],
      declarations: [ UserTableComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

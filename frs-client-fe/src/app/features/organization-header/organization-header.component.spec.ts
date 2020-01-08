import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OrganizationHeaderComponent } from './organization-header.component';

describe('OrganizationHeaderComponent', () => {
  let component: OrganizationHeaderComponent;
  let fixture: ComponentFixture<OrganizationHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OrganizationHeaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OrganizationHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

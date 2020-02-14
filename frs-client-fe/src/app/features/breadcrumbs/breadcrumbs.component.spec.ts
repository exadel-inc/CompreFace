import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {BreadcrumbsComponent} from './breadcrumbs.component';
import {CommonModule} from '@angular/common';
import {BreadcrumbsFacade} from './breadcrumbs.facade';
import {RouterModule} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {MatCardModule} from '@angular/material';

describe('BreadcrumbsComponent', () => {
  let component: BreadcrumbsComponent;
  let fixture: ComponentFixture<BreadcrumbsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BreadcrumbsComponent ],
      providers: [
        {
          provide: BreadcrumbsFacade,
          useValue: {}
        }
        ],
      imports: [
        CommonModule,
        RouterModule,
        RouterTestingModule,
        MatCardModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BreadcrumbsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

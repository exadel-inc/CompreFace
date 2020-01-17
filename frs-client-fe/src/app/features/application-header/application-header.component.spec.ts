import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationHeaderComponent } from './application-header.component';
import {CommonModule} from "@angular/common";
import {RouterModule} from "@angular/router";
import {MatButtonModule} from "@angular/material/button";
import {EntityTitleModule} from "../entity-title/entity-title.module";
import {ApplicationHeaderFacade} from "./application-header.facade";
import {SpinnerModule} from "../spinner/spinner.module";

describe('ApplicationHeaderComponent', () => {
  let component: ApplicationHeaderComponent;
  let fixture: ComponentFixture<ApplicationHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationHeaderComponent ],
      providers: [
        {
          provide: ApplicationHeaderFacade,
          useValue: {
            rename: () => {},
            initSubscriptions: () => {},
            unsubscribe: () => {},
          }
        },
      ],
      imports: [
        CommonModule,
        RouterModule,
        MatButtonModule,
        EntityTitleModule,
        SpinnerModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

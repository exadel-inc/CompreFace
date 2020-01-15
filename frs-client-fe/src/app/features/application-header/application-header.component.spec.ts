import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationHeaderComponent } from './application-header.component';
import {CommonModule} from "@angular/common";
import {RouterModule} from "@angular/router";
import {MatButtonModule} from "@angular/material/button";
import {EntityTitleModule} from "../entity-title/entity-title.module";
import {Subject} from "rxjs";
import {ApplicationHeaderFacade} from "./application-header.facade";

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
            select: () => {},
            rename: () => {},
            organization$: new Subject(),
            userRole$: new Subject(),
            selectedId$: new Subject(),
          }
        },
      ],
      imports: [
        CommonModule,
        RouterModule,
        MatButtonModule,
        EntityTitleModule
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

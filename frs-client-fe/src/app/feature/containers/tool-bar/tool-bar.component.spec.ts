import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ToolBarComponent } from './tool-bar.component';
import {CommonModule} from "@angular/common";
import {CustomMaterialModule} from "../../../ui/custom-material/custom-material.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {RouterTestingModule} from "@angular/router/testing";
import {MockStore} from "@ngrx/store/testing";
import {User} from "../../../data/user";
import {Store} from "@ngrx/store";
import {of} from "rxjs";

describe('ToolBarComponent', () => {
  let component: ToolBarComponent;
  let fixture: ComponentFixture<ToolBarComponent>;
  let store: MockStore<{ isAuthenticated: boolean, errorMessage: string, user: User}>;
  const initialState = { isAuthenticated: false, errorMessage: 'some error message', user: {}};

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ToolBarComponent ],
      providers: [
        {
          provide: Store,
          useValue: {
            dispatch: () => {},
            select: () => {
              return of(initialState)
            }
          }
        },
      ],
      imports: [
        CommonModule,
        CustomMaterialModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        BrowserAnimationsModule,
        RouterTestingModule
      ]
    })
    .compileComponents();

    store = TestBed.get<Store<any>>(Store);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ToolBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

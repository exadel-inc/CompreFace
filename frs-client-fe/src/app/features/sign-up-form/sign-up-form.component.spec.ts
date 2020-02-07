import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SignUpFormComponent} from './sign-up-form.component';
import {Store} from '@ngrx/store';
import {MockStore} from '@ngrx/store/testing';
import {of} from 'rxjs';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {RouterTestingModule} from '@angular/router/testing';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatDialogModule} from '@angular/material/dialog';

describe('SignUpFormComponent', () => {
  let component: SignUpFormComponent;
  let fixture: ComponentFixture<SignUpFormComponent>;
  let store: MockStore<{ isAuthenticated: boolean, errorMessage: string}>;
  const initialState = { isAuthenticated: false, errorMessage: 'some error message'};

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        SignUpFormComponent
      ],
      providers: [
        {
          provide: Store,
          useValue: {
            dispatch: () => {},
            select: () => {
              return of(initialState);
            }
          }
        },
      ],
      imports: [
        CommonModule,
        MatCardModule,
        MatInputModule,
        MatDialogModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        BrowserAnimationsModule,
        RouterTestingModule
      ]
    }).compileComponents();

    store = TestBed.get<Store<any>>(Store);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SignUpFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

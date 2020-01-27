import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationUserListComponent } from './application-user-list.component';
import { SpinnerModule } from '../spinner/spinner.module';
import { UserTableModule } from '../user-table/user-table.module';
import { ApplicationUserListFacade } from './application-user-list-facade';
import { of } from 'rxjs';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatFormFieldModule, MatInputModule, MatDialog } from '@angular/material';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('ApplicationUserListComponent', () => {
  let component: ApplicationUserListComponent;
  let fixture: ComponentFixture<ApplicationUserListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationUserListComponent ],
      imports: [SpinnerModule, UserTableModule, ReactiveFormsModule, FormsModule, MatFormFieldModule, MatInputModule, NoopAnimationsModule],
      providers: [{
        provide: MatDialog,
        useValue: {}
      },{
        provide: ApplicationUserListFacade,
        useValue: {
          initSubscriptions: () => of([{}]),
          appUsers$: of([{
            id: 0,
            name: 'name',
            owner: {
              firstname: 'firstname'
            }
          }]),
          selectedOrganization$: of([{}]),
          isLoading$: of([{}]),
          unsubscribe(){}
        }
      }]
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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SpinnerModule} from 'src/app/features/spinner/spinner.module';
import {UserTableModule} from 'src/app/features/user-table/user-table.module';
import {InviteUserComponent} from 'src/app/features/invite-user/invite-user.component';
import {UserListComponent} from './user-list.component';
import {MatDialog} from '@angular/material';
import {UserListFacade} from './user-list-facade';
import {of} from 'rxjs';
import {InviteUserModule} from '../invite-user/invite-user.module';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule} from '@angular/forms';
import {UserTablePipeModule} from '../../ui/search-pipe/user-table-filter.module';
import {MatInputModule} from '@angular/material/input';
import {SnackBarModule} from 'src/app/features/snackbar/snackbar.module';

describe('UserListComponent', () => {
  let component: UserListComponent;
  let fixture: ComponentFixture<UserListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SpinnerModule,
        UserTableModule,
        InviteUserModule,
        NoopAnimationsModule,
        FormsModule,
        UserTablePipeModule,
        MatInputModule,
        SnackBarModule
      ],
      declarations: [UserListComponent],
      providers: [
        {
          provide: MatDialog,
          useValue: {}
        }, {
          provide: UserListFacade,
          useValue: {
            initSubscriptions: () => of([{}]),
            users$: of([{
              id: 0,
              name: 'name',
              owner: {
                firstname: 'firstname'
              }
            }]),
            selectedOrganization$: of([{}]),
            isLoading$: of([{}]),
            unsubscribe() { }
          }
        }],
    })
      .overrideComponent(InviteUserComponent, {
        set: {}
      })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

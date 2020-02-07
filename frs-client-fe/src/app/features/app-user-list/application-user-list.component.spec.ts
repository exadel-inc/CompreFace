import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ApplicationUserListComponent} from './application-user-list.component';
import {SpinnerModule} from '../spinner/spinner.module';
import {UserTableModule} from '../user-table/user-table.module';
import {ApplicationUserListFacade} from './application-user-list-facade';
import {of} from 'rxjs';
import {MatDialog} from '@angular/material';
import {InviteUserModule} from '../invite-user/invite-user.module';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule} from '@angular/forms';
import {UserTablePipeModule} from '../../ui/search-pipe/user-table-filter.module';
import {MatInputModule} from '@angular/material/input';

describe('ApplicationUserListComponent', () => {
  let component: ApplicationUserListComponent;
  let fixture: ComponentFixture<ApplicationUserListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationUserListComponent ],
      imports: [
        SpinnerModule,
        UserTableModule,
        InviteUserModule,
        NoopAnimationsModule,
        FormsModule,
        UserTablePipeModule,
        MatInputModule
      ],
      providers: [{
        provide: MatDialog,
        useValue: {}
      }, {
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
          unsubscribe() {
          }
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

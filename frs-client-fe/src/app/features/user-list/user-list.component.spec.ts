import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { SpinnerModule } from 'src/app/features/spinner/spinner.module';
import { UserTableModule } from 'src/app/features/user-table/user-table.module';
import { InviteUserComponent } from 'src/app/features/invite-user/invite-user.component';
import { UserListComponent } from './user-list.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule, MatDialog, MatInputModule } from '@angular/material';
import { UserListFacade } from './user-list-facade';
import { of } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('UserListComponent', () => {
  let component: UserListComponent;
  let fixture: ComponentFixture<UserListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [SpinnerModule, UserTableModule, ReactiveFormsModule, FormsModule, MatFormFieldModule, MatInputModule, NoopAnimationsModule],
      declarations: [UserListComponent, InviteUserComponent],
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
        }]
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

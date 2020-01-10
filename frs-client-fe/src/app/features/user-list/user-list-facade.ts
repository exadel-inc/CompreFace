import { Injectable } from '@angular/core';
import { IFacade } from 'src/app/core/facade/IFacade';
import { AppUser } from 'src/app/data/appUser';
import { AppState } from 'src/app/store';
import { Observable, Subscription } from 'rxjs';
import { Store } from '@ngrx/store';
import { getSelectedOrganizationId } from 'src/app/store/organization/selectors';
import { selectUsers } from 'src/app/store/user/selectors';
import { FetchUsers, UpdateUserRole } from 'src/app/store/userList/actions';
import { selectUserListState } from 'src/app/store/userList/selectors';
import { UserListState } from 'src/app/store/userList/reducers';

@Injectable()
export class UserListFacade implements IFacade {
  public selectedOrganization$: Observable<string>;
  public userListState$: Observable<UserListState>;
  public users$: Observable<AppUser[]>;
  private selectedOrganization: string;

  private selectedOrganizationSubscription: Subscription;

  constructor(private store: Store<AppState>) {
    this.userListState$ = store.select(selectUserListState);
    this.selectedOrganization$ = store.select(getSelectedOrganizationId);
    this.users$ = store.select(selectUsers);
  }

  public initSubscriptions(): void {
    this.selectedOrganizationSubscription = this.selectedOrganization$.subscribe(
      orgId => {
        if (orgId) {
          this.selectedOrganization = orgId;
          this.loadUsers();
        }
      }
    );
  }

  public loadUsers(): void {
    this.store.dispatch(new FetchUsers({
      organizationId: this.selectedOrganization
    }));
  }

  public updateUserRole(id, accessLevel): void {
    this.store.dispatch(new UpdateUserRole({
      organizationId: this.selectedOrganization,
      id,
      accessLevel
    }));
  }

  public unsubscribe(): void {
    this.selectedOrganizationSubscription.unsubscribe();
  }
}

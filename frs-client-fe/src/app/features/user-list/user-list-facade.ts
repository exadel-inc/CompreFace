import {Injectable} from '@angular/core';
import {IFacade} from 'src/app/data/facade/IFacade';
import {AppUser} from 'src/app/data/appUser';
import {AppState} from 'src/app/store';
import {Observable, Subscription, combineLatest} from 'rxjs';
import {Store} from '@ngrx/store';
import {selectCurrentOrganizationId} from 'src/app/store/organization/selectors';
import {selectUsers, selectIsPendingUserStore} from 'src/app/store/user/selectors';
import {selectAllRoles, selectIsPendingRoleStore} from 'src/app/store/role/selectors';
import {selectUserRollForSelectedOrganization, selectSelectedOrganization} from 'src/app/store/organization/selectors';
import {map, tap} from 'rxjs/operators';
import {LoadUsersEntityAction, PutUpdatedUserRoleEntityAction, DeleteUserFromOrganization} from 'src/app/store/user/action';
import {LoadRolesEntityAction} from 'src/app/store/role/actions';
import {UserService} from 'src/app/core/user/user.service';

@Injectable()
export class UserListFacade implements IFacade {
  public selectedOrganization$: Observable<string>;
  public selectedOrganizationName$: Observable<string>;
  public users$: Observable<AppUser[]>;
  public availableRoles$: Observable<string[]>;
  public isLoading$: Observable<boolean>;

  private selectedOrganization: string;

  private selectedOrganizationSubscription: Subscription;

  constructor(private store: Store<AppState>, private userService: UserService) {
    this.selectedOrganization$ = store.select(selectCurrentOrganizationId);
    this.selectedOrganizationName$ = store.select(selectSelectedOrganization).pipe(map(org => org.name));
    this.users$ = store.select(selectUsers);

    const allRoles$ = store.select(selectAllRoles);
    const userRoleInSelectedOrganization$ = store.select(selectUserRollForSelectedOrganization);

    this.availableRoles$ = combineLatest(
      allRoles$,
      userRoleInSelectedOrganization$
    )
      .pipe(
        map(
          (observResult) => {
            let res = [];
            const [allRoles, userRole] = observResult;
            const roleIndex = allRoles.indexOf(userRole);

            if (roleIndex !== -1) {
              res = allRoles.slice(roleIndex);
            }

            return res;
          }
        )
      );

    const usersLoading$ = store.select(selectIsPendingUserStore);
    const roleLoading$ = store.select(selectIsPendingRoleStore);

    this.isLoading$ = combineLatest(usersLoading$, roleLoading$)
          .pipe(map(observResults => !(!observResults[0] && !observResults[1])));
  }

  public initSubscriptions(): void {
    this.selectedOrganizationSubscription = this.selectedOrganization$.subscribe(
      orgId => {
        if (orgId) {
          this.selectedOrganization = orgId;
          this.loadUsers();
          this.loadAvailableRoles();
        }
      }
    );
  }

  public loadUsers(): void {
    this.store.dispatch(LoadUsersEntityAction({
      organizationId: this.selectedOrganization
    }));
  }

  public updateUserRole(id: string, role: string): void {
    this.store.dispatch(PutUpdatedUserRoleEntityAction({
      organizationId: this.selectedOrganization,
      user: {
        id,
        role
      }
    }));
  }

  public deleteUser(userId: string): void {
    this.store.dispatch(DeleteUserFromOrganization({
      organizationId: this.selectedOrganization,
      userId
    }))
  }

  public inviteUser(userEmail: string, role: string): Observable<any> {
    return this.userService.inviteUser(this.selectedOrganization, userEmail, role)
      .pipe(tap(() => this.loadUsers()));
  }

  public loadAvailableRoles(): void {
    this.store.dispatch(LoadRolesEntityAction());
  }

  public unsubscribe(): void {
    this.selectedOrganizationSubscription.unsubscribe();
  }
}

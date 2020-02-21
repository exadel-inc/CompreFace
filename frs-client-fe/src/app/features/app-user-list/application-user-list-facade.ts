import {Injectable} from '@angular/core';
import {IFacade} from 'src/app/data/facade/IFacade';
import {Store} from '@ngrx/store';
import {selectAppUserIsPending, selectAppUsers} from 'src/app/store/app-user/selectors';
import {loadAppUserEntityAction, putUpdatedAppUserRoleEntityAction} from 'src/app/store/app-user/actions';
import {selectCurrentAppId, selectUserRollForSelectedApp} from 'src/app/store/application/selectors';
import {AppState} from 'src/app/store';
import {Observable, combineLatest, Subscription, zip} from 'rxjs';
import {AppUser} from 'src/app/data/appUser';
import {selectAllRoles, selectIsPendingRoleStore} from 'src/app/store/role/selectors';
import {map, tap} from 'rxjs/operators';
import {selectCurrentOrganizationId} from 'src/app/store/organization/selectors';
import {LoadRolesEntityAction} from 'src/app/store/role/actions';
import {selectUsers} from '../../store/user/selectors';
import {LoadUsersEntityAction} from '../../store/user/action';
import {AppUserService} from '../../core/app-user/app-user.service';

@Injectable()
export class ApplicationUserListFacade implements IFacade {
  public isLoading$: Observable<boolean>;
  public appUsers$: Observable<AppUser[]>;
  public availableRoles$: Observable<string[]>;
  public availableEmails$: Observable<string[]>;

  private selectedApplicationId: string;
  private selectedOrganizationId: string;
  private sub: Subscription;

  constructor(private store: Store<AppState>, private userService: AppUserService) {
    this.appUsers$ = store.select(selectAppUsers);
    this.availableEmails$ = store.select(selectUsers).pipe(map(data => data.map(user => user.email)));
    const allRoles$ = store.select(selectAllRoles);
    const currentApplicationUserRole$ = store.select(selectUserRollForSelectedApp);

    this.availableRoles$ = combineLatest(
      allRoles$,
      currentApplicationUserRole$
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

    const usersLoading$ = store.select(selectAppUserIsPending);
    const roleLoading$ = store.select(selectIsPendingRoleStore);

    this.isLoading$ = combineLatest(usersLoading$, roleLoading$)
      .pipe(
        map(observResults => !(!observResults[0] && !observResults[1])
        ));
  }

  public initSubscriptions(): void {
    this.sub = zip(
      this.store.select(selectCurrentAppId),
      this.store.select(selectCurrentOrganizationId)
    ).subscribe(([appId, orgId]) => {
      if (appId && orgId) {
        this.selectedApplicationId = appId;
        this.selectedOrganizationId = orgId;
        this.loadData();
      }
    });
  }

  public loadData(): void {
    this.store.dispatch(loadAppUserEntityAction({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId
    }));
    this.store.dispatch(LoadRolesEntityAction());
    this.store.dispatch(LoadUsersEntityAction({
      organizationId: this.selectedOrganizationId
    }));
  }

  public updateUserRole(id: string, role: string): void {
    this.store.dispatch(putUpdatedAppUserRoleEntityAction({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      user: {
        id,
        role
      }
    }));
  }

  public inviteUser(email: string, role: string): Observable<any> {
    return this.userService.inviteUser(this.selectedOrganizationId, this.selectedApplicationId, email, role)
      .pipe(tap(() =>
        this.store.dispatch(loadAppUserEntityAction({
          organizationId: this.selectedOrganizationId,
          applicationId: this.selectedApplicationId
        }))
      ));
  }

  public unsubscribe(): void {
    this.sub.unsubscribe();
  }
}

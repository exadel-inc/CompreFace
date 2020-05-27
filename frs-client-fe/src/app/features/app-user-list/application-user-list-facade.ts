import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { combineLatest, Observable, Subscription, zip } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';
import { IFacade } from 'src/app/data/facade/IFacade';
import { AppState } from 'src/app/store';
import { loadAppUserEntityAction, putUpdatedAppUserRoleEntityAction } from 'src/app/store/app-user/actions';
import { selectAppUserIsPending, selectAppUsers } from 'src/app/store/app-user/selectors';
import { selectCurrentAppId, selectUserRollForSelectedApp } from 'src/app/store/application/selectors';
import { selectCurrentOrganizationId } from 'src/app/store/organization/selectors';
import { LoadRolesEntityAction } from 'src/app/store/role/actions';
import { selectAllRoles, selectIsPendingRoleStore } from 'src/app/store/role/selectors';

import { AppUserService } from '../../core/app-user/app-user.service';
import { LoadUsersEntityAction } from '../../store/user/action';
import { selectUsers } from '../../store/user/selectors';

@Injectable()
export class ApplicationUserListFacade implements IFacade {
  isLoading$: Observable<boolean>;
  appUsers$: Observable<AppUser[]>;
  availableRoles$: Observable<string[]>;
  availableEmails$: Observable<string[]>;
  userRole$: Observable<string>;

  private selectedApplicationId: string;
  private selectedOrganizationId: string;
  private sub: Subscription;

  constructor(private store: Store<AppState>, private userService: AppUserService) {
    this.appUsers$ = store.select(selectAppUsers);
    this.availableEmails$ = store.select(selectUsers).pipe(map(data => data.map(user => user.email)));
    this.userRole$ = store.select(selectUserRollForSelectedApp);
    const allRoles$ = store.select(selectAllRoles);

    this.availableRoles$ = combineLatest(
      allRoles$,
      this.userRole$
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

  initSubscriptions(): void {
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

  loadData(): void {
    this.store.dispatch(loadAppUserEntityAction({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId
    }));
    this.store.dispatch(LoadRolesEntityAction());
    this.store.dispatch(LoadUsersEntityAction({
      organizationId: this.selectedOrganizationId
    }));
  }

  updateUserRole(id: string, role: string): void {
    this.store.dispatch(putUpdatedAppUserRoleEntityAction({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      user: {
        id,
        role
      }
    }));
  }

  inviteUser(email: string, role: string): Observable<any> {
    return this.userService.inviteUser(this.selectedOrganizationId, this.selectedApplicationId, email, role)
      .pipe(tap(() =>
        this.store.dispatch(loadAppUserEntityAction({
          organizationId: this.selectedOrganizationId,
          applicationId: this.selectedApplicationId
        }))
      ));
  }

  unsubscribe(): void {
    this.sub.unsubscribe();
  }
}

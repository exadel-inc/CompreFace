import { Injectable } from '@angular/core';
import { IFacade } from 'src/app/core/facade/IFacade';
import { Store } from '@ngrx/store';
import { selectAppUserIsPending, selectAppUsers } from 'src/app/store/app-user/selectors';
import { loadAppUserEntityAction, putUpdatedAppUserRoleEntityAction } from 'src/app/store/app-user/actions';
import { selectCurrentAppId, selectUserRollForSelectedApp } from 'src/app/store/application/selectors';
import { AppState } from 'src/app/store';
import { Observable, combineLatest, Subscription } from 'rxjs';
import { AppUser } from 'src/app/data/appUser';
import { selectAllRoles, selectIsPendingRoleStore } from 'src/app/store/role/selectors';
import { map } from 'rxjs/operators';
import { selectCurrentOrganizationId } from 'src/app/store/organization/selectors';
import { LoadRolesEntityAction } from 'src/app/store/role/actions';

@Injectable()
export class ApplicationUserListFacade implements IFacade {
  public isLoading$: Observable<boolean>;
  public appUsers$: Observable<AppUser[]>;
  public availableRoles$: Observable<string[]>;

  private selectedApplicationId: string;
  private selectedApplicationIdSubscription: Subscription;

  private selectedOrganizationId: string;
  private selectedOrganizationIdSubscription: Subscription;

  constructor(private store: Store<AppState>) {
    this.appUsers$ = store.select(selectAppUsers);

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

            if (~roleIndex) {
              res = allRoles.slice(roleIndex);
            }

            return res;
          }
        )
      );

    const usersLoading$ = store.select(selectAppUserIsPending);
    const roleLoading$ = store.select(selectIsPendingRoleStore);

    this.isLoading$ = combineLatest(usersLoading$, roleLoading$)
          .pipe(map(observResults => !(!observResults[0] && !observResults[1])));

  }

  public initSubscriptions(): void {
    this.selectedApplicationIdSubscription = this.store.select(selectCurrentAppId).subscribe(id => {
      if (id) {
        this.selectedApplicationId = id;
        this.loadData();
      }
    });
    this.selectedOrganizationIdSubscription = this.store.select(selectCurrentOrganizationId).subscribe(id => {
      if (id) {
        this.selectedOrganizationId = id;
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
  }

  public updateUserRole(id: string, accessLevel: string): void {
    this.store.dispatch(putUpdatedAppUserRoleEntityAction({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      user: {
        id,
        accessLevel
      }
    }));
  }

  public unsubscribe(): void {
    this.selectedApplicationIdSubscription.unsubscribe();
    this.selectedOrganizationIdSubscription.unsubscribe();
  }
}

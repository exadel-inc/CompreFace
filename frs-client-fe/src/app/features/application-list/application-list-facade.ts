import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { Application } from 'src/app/data/application';
import { IFacade } from 'src/app/data/facade/IFacade';
import { AppState } from 'src/app/store';
import { createApplication, loadApplications } from 'src/app/store/application/action';
import { selectApplications, selectIsPendingApplicationList } from 'src/app/store/application/selectors';
import { selectCurrentOrganizationId, selectUserRollForSelectedOrganization } from 'src/app/store/organization/selectors';

@Injectable()
export class ApplicationListFacade implements IFacade {
  applications$: Observable<Application[]>;
  selectedOrganizationId$: Observable<string>;
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;

  private selectedOrganizationIdSubscription: Subscription;
  private selectedOrgId: string;

  constructor(private store: Store<AppState>) {
    this.applications$ = store.select(selectApplications);
    this.selectedOrganizationId$ = store.select(selectCurrentOrganizationId);
    this.userRole$ = this.store.select(selectUserRollForSelectedOrganization);

    this.isLoading$ = store.select(selectIsPendingApplicationList);
  }

  initSubscriptions(): void {
    this.selectedOrganizationIdSubscription = this.selectedOrganizationId$.subscribe(
      organizationId => {
        if (organizationId) {
          this.selectedOrgId = organizationId;
          this.loadApplications();
        }
      }
    );
  }

  loadApplications(): void {
    this.store.dispatch(
      loadApplications({ organizationId: this.selectedOrgId })
    );
  }

  createApplication(name: string): void {
    this.store.dispatch(
      createApplication({ organizationId: this.selectedOrgId, name })
    );
  }

  unsubscribe(): void {
    this.selectedOrganizationIdSubscription.unsubscribe();
  }

  getOrgId(): string {
    return this.selectedOrgId;
  }
}

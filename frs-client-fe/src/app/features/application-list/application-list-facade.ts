import { Injectable } from '@angular/core';
import { IFacade } from 'src/app/core/facade/IFacade';
import { AppState } from 'src/app/store';
import { Store } from '@ngrx/store';
import { selectApplications, selectIsPendingApplicationList } from 'src/app/store/application/selectors';
import { getSelectedOrganizationId } from 'src/app/store/organization/selectors';
import { Observable, Subscription } from 'rxjs';
import { Application } from 'src/app/data/application';
import { loadApplicationsEntityAction, createApplicationEntityAction } from 'src/app/store/application/action';

@Injectable()
export class ApplicationListFacade implements IFacade {
  public applications$: Observable<Application[]>;
  public selectedOrganization$: Observable<string>;
  public isLoading$: Observable<boolean>;

  private selectedOrganizationSubscription: Subscription;
  private selectedOrgId: string;

  constructor(private store: Store<AppState>) {
    this.applications$ = store.select(selectApplications);
    this.selectedOrganization$ = store.select(getSelectedOrganizationId);

    this.isLoading$ = store.select(selectIsPendingApplicationList);
  }

  public initSubscriptions(): void {
    this.selectedOrganizationSubscription = this.selectedOrganization$.subscribe(
      organizationId => {
        if (organizationId) {
          this.selectedOrgId = organizationId;
          this.loadApplications();
        }
      }
    );
  }

  public loadApplications(): void {
    this.store.dispatch(
      loadApplicationsEntityAction({ organizationId: this.selectedOrgId })
    );
  }

  public createApplication(name: string): void {
    this.store.dispatch(
      createApplicationEntityAction({ organizationId: this.selectedOrgId, name })
    )
  }

  public unsubscribe(): void {
    this.selectedOrganizationSubscription.unsubscribe();
  }

  public getOrgId():string {
    return this.selectedOrgId;
  }
}

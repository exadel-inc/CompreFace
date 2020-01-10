import { Injectable } from '@angular/core';
import { IFacade } from 'src/app/core/facade/IFacade';
import { AppState } from 'src/app/store';
import { ApplicationListState } from 'src/app/store/applicationList/reducers';
import { Store } from '@ngrx/store';
import { selectApplications } from 'src/app/store/application/selectors';
import { selectApplicationListState } from 'src/app/store/applicationList/selectors';
import { getSelectedOrganizationId } from 'src/app/store/organization/selectors';
import { Observable, Subscription } from 'rxjs';
import { Application } from 'src/app/data/application';
import { FetchApplicationList, CreateApplication } from 'src/app/store/applicationList/action';

@Injectable()
export class ApplicationListFacade implements IFacade {
  public applications$: Observable<Application[]>;
  public applicationListState$: Observable<ApplicationListState>;
  public selectedOrganization$: Observable<string>;

  private selectedOrganizationSubscription: Subscription;
  private selectedOrgId: string;

  constructor(private store: Store<AppState>) {
    this.applications$ = store.select(selectApplications);
    this.applicationListState$ = store.select(selectApplicationListState);
    this.selectedOrganization$ = store.select(getSelectedOrganizationId);
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
      new FetchApplicationList({ organizationId: this.selectedOrgId })
    );
  }

  public createApplication(name: string): void {
    this.store.dispatch(
      new CreateApplication({ organizationId: this.selectedOrgId, name })
    )
  }

  public unsubscribe(): void {
    this.selectedOrganizationSubscription.unsubscribe();
  }
}

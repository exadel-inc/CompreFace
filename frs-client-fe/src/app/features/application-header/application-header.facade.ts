import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';

import { Application } from '../../data/application';
import { IFacade } from '../../data/facade/IFacade';
import { AppState } from '../../store';
import { deleteApplication, updateApplication } from '../../store/application/action';
import {
  selectCurrentApp,
  selectCurrentAppId,
  selectIsPendingApplicationList,
  selectUserRollForSelectedApp,
} from '../../store/application/selectors';
import { selectCurrentOrganizationId } from '../../store/organization/selectors';

@Injectable()
export class ApplicationHeaderFacade implements IFacade {
  selectedId$: Observable<string | null>;
  selectedId: string | null;
  loading$: Observable<boolean>;
  userRole$: Observable<string | null>;
  app$: Observable<Application>;
  orgId: string;
  orgIdSub: Subscription;
  appIdSub: Subscription;

  constructor(private store: Store<AppState>) {
    this.app$ = this.store.select(selectCurrentApp);
    this.userRole$ = this.store.select(selectUserRollForSelectedApp);
    this.selectedId$ = this.store.select(selectCurrentAppId);
    this.loading$ = this.store.select(selectIsPendingApplicationList);
  }

  initSubscriptions() {
    this.orgIdSub = this.store.select(selectCurrentOrganizationId).subscribe(orgId => this.orgId = orgId);
    this.appIdSub = this.selectedId$.subscribe(selectedId => this.selectedId = selectedId);
  }

  unsubscribe() {
    this.orgIdSub.unsubscribe();
    this.appIdSub.unsubscribe();
  }

  rename(name: string) {
    this.store.dispatch(updateApplication({ name, id: this.selectedId, organizationId: this.orgId }));
  }

  delete() {
    this.store.dispatch(deleteApplication({ id: this.selectedId, organizationId: this.orgId }));
  }
}

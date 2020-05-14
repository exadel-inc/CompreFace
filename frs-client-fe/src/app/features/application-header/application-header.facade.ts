import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {Observable, Subscription} from 'rxjs';
import {AppState} from '../../store';
import {IFacade} from '../../data/facade/IFacade';
import {
  selectCurrentApp,
  selectCurrentAppId,
  selectUserRollForSelectedApp,
  selectIsPendingApplicationList
} from '../../store/application/selectors';
import {Application} from '../../data/application';
import {updateApplication} from '../../store/application/action';
import {selectCurrentOrganizationId} from '../../store/organization/selectors';

@Injectable()
export class ApplicationHeaderFacade implements IFacade {
  selectedId$: Observable<string | null>;
  selectedId: string | null;
  loading$: Observable<boolean>;
  public userRole$: Observable<string | null>;
  public app$: Observable<Application>;
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
}

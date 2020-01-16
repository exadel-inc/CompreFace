import { Injectable } from '@angular/core';
import {Store} from "@ngrx/store";
import {Observable, Subscription} from "rxjs";
import {AppState} from "../../store";
import {IFacade} from "../../core/facade/IFacade";
import {selectCurrentApp, selectCurrentAppId, SelectUserRollForSelectedApp} from "../../store/application/selectors";
import {Application} from "../../data/application";
import {isLoading} from "../../store/applicationList/selectors";
import {UpdateApplication} from "../../store/applicationList/action";
import {getSelectedOrganizationId} from "../../store/organization/selectors";

@Injectable()
export class ApplicationHeaderFacade implements IFacade{
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
    this.userRole$ = this.store.select(SelectUserRollForSelectedApp);
    this.selectedId$ = this.store.select(selectCurrentAppId);
    this.loading$ = this.store.select(isLoading);
  }

  initSubscriptions() {
    this.orgIdSub = this.store.select(getSelectedOrganizationId).subscribe(orgId => this.orgId = orgId);
    this.appIdSub = this.selectedId$.subscribe(selectedId => this.selectedId = selectedId);
  }

  unsubscribe() {
    this.orgIdSub.unsubscribe();
    this.appIdSub.unsubscribe();
  }

  rename(name: string) {
    this.store.dispatch(new UpdateApplication({name, appId: this.selectedId, organizationId: this.orgId}));
  }
}

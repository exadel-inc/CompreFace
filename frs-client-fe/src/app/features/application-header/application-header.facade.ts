import { Injectable } from '@angular/core';
import {Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {AppState} from "../../store";
import {IFacade} from "../../core/facade/IFacade";
import {selectCurrentApp, selectCurrentAppId, SelectUserRollForSelectedApp} from "../../store/application/selectors";
import {Application} from "../../data/application";
import {isLoadingApps} from "../../store/applicationList/selectors";

@Injectable()
export class ApplicationHeaderFacade implements IFacade{
  selectedId$: Observable<string | number | null>;
  loading$: Observable<boolean>;
  public userRole$: Observable<string | null>;
  public app$: Observable<Application>;

  constructor(private store: Store<AppState>) {
    this.app$ = this.store.select(selectCurrentApp);
    this.userRole$ = this.store.select(SelectUserRollForSelectedApp);
    this.selectedId$ = this.store.select(selectCurrentAppId);
    this.loading$ = this.store.select(isLoadingApps);
  }

  initSubscriptions() {}

  unsubscribe() {}

  rename(name: string) {
    // this.organizationEnService.update({name, id: this.selectedId})
  }
}

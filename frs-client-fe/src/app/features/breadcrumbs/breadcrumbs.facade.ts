import { Injectable } from '@angular/core';
import {SelectSelectedOrganization,} from "../../store/organization/selectors";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {Organization} from "../../data/organization";
import {AppState} from "../../store";
import {Application} from "../../data/application";
import {selectCurrentApp} from "../../store/application/selectors";

@Injectable()
export class BreadcrumbsFacade{
  org$: Observable<Organization>;
  app$: Observable<Application>;

  constructor(private store: Store<AppState>) {
    this.org$ = this.store.select(SelectSelectedOrganization);
    this.app$ = this.store.select(selectCurrentApp);
  }
}

import { Injectable } from '@angular/core';
import { selectSelectedOrganization } from "../../store/organization/selectors";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import { Organization } from "../../data/organization";
import { AppState } from "../../store";
import { Application } from "../../data/application";
import { selectCurrentApp } from "../../store/application/selectors";
import {selectCurrentModel} from "../../store/model/selectors";
import {Model} from "../../data/model";

@Injectable()
export class BreadcrumbsFacade {
  org$: Observable<Organization>;
  app$: Observable<Application>;
  model$: Observable<Model>;

  constructor(private store: Store<AppState>) {
    this.org$ = this.store.select(selectSelectedOrganization);
    this.app$ = this.store.select(selectCurrentApp);
    this.model$ = this.store.select(selectCurrentModel);
  }
}

import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { Application } from '../../data/application';
import { Organization } from '../../data/organization';
import { AppState } from '../../store';
import { selectCurrentApp } from '../../store/application/selectors';
import { selectSelectedOrganization } from '../../store/organization/selectors';

@Injectable()
export class BreadcrumbsFacade {
  org$: Observable<Organization>;
  app$: Observable<Application>;

  constructor(private store: Store<AppState>) {
    this.org$ = this.store.select(selectSelectedOrganization);
    this.app$ = this.store.select(selectCurrentApp);
  }
}

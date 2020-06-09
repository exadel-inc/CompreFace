/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

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

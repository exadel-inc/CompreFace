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
import { Application } from '../../data/interfaces/application';
import { IFacade } from '../../data/interfaces/IFacade';
import { AppState } from '../../store';
import { deleteApplication, updateApplication } from '../../store/application/action';
import { selectCurrentApp, selectCurrentAppId } from '../../store/application/selectors';
import { selectIsLoadingApplicationList } from '../../store/user/selectors';
import { selectUserRole } from '../../store/app-user/selectors';

@Injectable()
export class ApplicationHeaderFacade implements IFacade {
  selectedId$: Observable<string | null>;
  selectedId: string | null;
  isLoadingAppList$: Observable<boolean>;
  userRole$: Observable<string | null>;
  app$: Observable<Application>;
  appIdSub: Subscription;

  constructor(private store: Store<AppState>) {
    this.app$ = store.select(selectCurrentApp);
    this.selectedId$ = store.select(selectCurrentAppId);
    this.isLoadingAppList$ = store.select(selectIsLoadingApplicationList);
    this.userRole$ = store.select(selectUserRole);
  }

  initSubscriptions() {
    this.appIdSub = this.selectedId$.subscribe(selectedId => (this.selectedId = selectedId));
  }

  unsubscribe() {
    this.appIdSub.unsubscribe();
  }

  rename(name: string) {
    this.store.dispatch(updateApplication({ name, id: this.selectedId }));
  }

  delete() {
    this.store.dispatch(deleteApplication({ id: this.selectedId }));
  }
}

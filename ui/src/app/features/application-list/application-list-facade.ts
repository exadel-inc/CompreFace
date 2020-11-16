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
import { Application } from 'src/app/data/interfaces/application';
import { IFacade } from 'src/app/data/interfaces/IFacade';
import { AppState } from 'src/app/store';
import { createApplication, loadApplications } from 'src/app/store/application/action';
import { selectApplications, selectIsPendingApplicationList } from 'src/app/store/application/selectors';

@Injectable()
export class ApplicationListFacade implements IFacade {
  applications$: Observable<Application[]>;
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;

  constructor(private store: Store<AppState>) {
    this.applications$ = store.select(selectApplications);
    this.isLoading$ = store.select(selectIsPendingApplicationList);
  }

  initSubscriptions(): void {
    this.loadApplications();
  }

  loadApplications(): void {
    this.store.dispatch(loadApplications());
  }

  createApplication(name: string): void {
    this.store.dispatch(createApplication({ name }));
  }

  unsubscribe(): void {}
}

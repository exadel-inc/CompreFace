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
import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';

import {Application} from '../../data/application';
import {AppState} from '../../store';
import {selectCurrentApp} from '../../store/application/selectors';
import {Model} from '../../data/model';
import {selectCurrentModel} from '../../store/model/selectors';
import {selectCurrentOrganizationId} from '../../store/organization/selectors';

@Injectable()
export class BreadcrumbsFacade {
  orgId$: Observable<string>;
  app$: Observable<Application>;
  model$: Observable<Model>;

  constructor(private store: Store<AppState>) {
    this.orgId$ = this.store.select(selectCurrentOrganizationId);
    this.app$ = this.store.select(selectCurrentApp);
    this.model$ = this.store.select(selectCurrentModel);
  }
}

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
import { Observable } from 'rxjs';
import { filter } from 'rxjs/operators';
import { Application } from 'src/app/data/interfaces/application';
import { Model } from 'src/app/data/interfaces/model';
import { Statistics } from 'src/app/data/interfaces/statistics';
import { selectCurrentAppId } from 'src/app/store/application/selectors';
import { loadModel, setSelectedModelIdEntityAction } from 'src/app/store/model/action';
import { selectCurrentModel, selectCurrentModelId } from 'src/app/store/model/selectors';
import { loadModelStatistics } from 'src/app/store/statistics/actions';
import { selectModelStatistics } from 'src/app/store/statistics/selectors';

@Injectable()
export class ModelInfoFacade {
  currentApp$: Observable<Application>;
  currentModel$: Observable<Model>;
  statistics$: Observable<Statistics[]>;
  selectCurrentAppId$: Observable<string>;
  selectCurrentModelId$: Observable<string>;

  constructor(private store: Store<any>) {
    this.currentModel$ = this.store.select(selectCurrentModel).pipe(filter(model => !!model));
    this.statistics$ = this.store.select(selectModelStatistics).pipe(filter(data => !!data[0]));
    this.selectCurrentAppId$ = this.store.select(selectCurrentAppId);
    this.selectCurrentModelId$ = this.store.select(selectCurrentModelId);
  }

  loadModelStatistics(appId: string, modelId: string): void {
    this.store.dispatch(loadModelStatistics({ appId, modelId }));
  }
}

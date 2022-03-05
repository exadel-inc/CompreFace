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
import { Model } from 'src/app/data/interfaces/model';
import { loadModels, setSelectedModelIdEntityAction } from 'src/app/store/model/action';
import { selectCurrentModel } from 'src/app/store/model/selectors';

@Injectable()
export class ModelInfoFacade {
  currentModel$: Observable<Model>;

  constructor(private store: Store<any>) {
    this.currentModel$ = this.store.select(selectCurrentModel).pipe(filter(model => !!model));
  }

  loadTotalImagesInfo(applicationId: string, selectedModelId: string): void {
    this.store.dispatch(loadModels({ applicationId }));
    this.store.dispatch(setSelectedModelIdEntityAction({ selectedModelId }));
  }
}

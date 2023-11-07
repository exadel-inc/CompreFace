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
import { Actions, Effect, ofType } from '@ngrx/effects';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap, tap, withLatestFrom } from 'rxjs/operators';
import { ModelService } from 'src/app/core/model/model.service';
import { SnackBarService } from 'src/app/features/snackbar/snackbar.service';
import { Router } from '@angular/router';
import { Routes } from '../../data/enums/routers-url.enum';

import {
  createModel,
  createModelFail,
  createModelSuccess,
  cloneModel,
  cloneModelFail,
  cloneModelSuccess,
  deleteModel,
  deleteModelFail,
  deleteModelSuccess,
  loadModels,
  loadModelsFail,
  loadModelsSuccess,
  updateModel,
  updateModelFail,
  updateModelSuccess,
  loadModel,
  loadModelFail,
  loadModelSuccess,
} from './action';
import { ServiceTypes } from 'src/app/data/enums/service-types.enum';
import { Store } from '@ngrx/store';
import { selectCurrentApp } from '../application/selectors';

@Injectable()
export class ModelEffects {
  constructor(
    private actions: Actions,
    private modelService: ModelService,
    private snackBarService: SnackBarService,
    private router: Router,
    private store: Store<any>
  ) {}

  @Effect()
  loadModel$ = this.actions.pipe(
    ofType(loadModel),
    switchMap(action =>
      this.modelService.getModel(action.applicationId, action.selectedModelId).pipe(
        map(model => loadModelSuccess({ model })),
        catchError(error => {
          return of(loadModelFail({ error }));
        })
      )
    )
  );

  @Effect()
  loadModels$ = this.actions.pipe(
    ofType(loadModels),
    switchMap(action =>
      this.modelService.getAll(action.applicationId).pipe(
        map(models => loadModelsSuccess({ models })),
        catchError(error => of(loadModelsFail({ error })))
      )
    )
  );

  isFirtsService: boolean;

  @Effect()
  createModel$ = this.actions.pipe(
    ofType(createModel),
    tap(({ model }) => (this.isFirtsService = model.isFirstService)),
    switchMap(action =>
      this.modelService.create(action.model.applicationId, action.model.name, action.model.type).pipe(
        map(model => createModelSuccess({ model })),
        catchError(error => of(createModelFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  createModelSuccess$ = this.actions.pipe(
    ofType(createModelSuccess),
    withLatestFrom(this.store.select(selectCurrentApp)),
    tap(([{ model }, app]) => {
      if (this.isFirtsService) {
        model.type === ServiceTypes.Recognition
          ? this.router.navigate([Routes.ManageCollection], {
              queryParams: {
                app: app.id,
                model: model.id,
                type: model.type,
              },
            })
          : this.router.navigate([Routes.TestModel], {
              queryParams: {
                app: app.id,
                model: model.id,
                type: model.type,
              },
            });
      }
    })
  );

  @Effect()
  updateModel$ = this.actions.pipe(
    ofType(updateModel),
    switchMap(action =>
      this.modelService.update(action.applicationId, action.modelId, action.name).pipe(
        map(model => updateModelSuccess({ model })),
        catchError(error => of(updateModelFail({ error })))
      )
    )
  );

  @Effect()
  cloneModel$ = this.actions.pipe(
    ofType(cloneModel),
    switchMap(action =>
      this.modelService.clone(action.applicationId, action.modelId, action.name).pipe(
        map(model => cloneModelSuccess({ model })),
        catchError(error => of(cloneModelFail({ error })))
      )
    )
  );

  @Effect()
  deleteModel$ = this.actions.pipe(
    ofType(deleteModel),
    switchMap(action =>
      forkJoin([of(action.modelId), this.modelService.delete(action.applicationId, action.modelId)]).pipe(
        map(([modelId]) => deleteModelSuccess({ modelId })),
        catchError(error => of(deleteModelFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(loadModelsFail, createModelFail, cloneModelFail, updateModelFail, deleteModelFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );

  //Listen for the 'loadModelsFail'
  @Effect({ dispatch: false })
  loadFail$ = this.actions.pipe(
    ofType(loadModelsFail),
    tap(() => {
      this.router.navigateByUrl(Routes.Home);
    })
  );
}

import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { ModelService } from 'src/app/core/model/model.service';
import { SnackBarService } from 'src/app/features/snackbar/snackbar.service';

import {
  createModel,
  createModelFail,
  createModelSuccess,
  deleteModel,
  deleteModelFail,
  deleteModelSuccess,
  loadModels,
  loadModelsFail,
  loadModelsSuccess,
  setSelectedIdModel,
  updateModel,
  updateModelFail,
  updateModelSuccess,
} from './actions';

@Injectable()
export class ModelEffects {
  constructor(
    private actions: Actions,
    private modelService: ModelService,
    private snackBarService: SnackBarService,
  ) { }

  @Effect()
  loadModels$ = this.actions.pipe(
    ofType(loadModels),
    switchMap(action => this.modelService.getAll(action.organizationId, action.applicationId).pipe(
      map(models => loadModelsSuccess({ models })),
      catchError(error => of(loadModelsFail({ error }))),
    )),
  );

  @Effect()
  createModel$ = this.actions.pipe(
    ofType(createModel),
    switchMap(action => this.modelService.create(action.organizationId, action.applicationId, action.name).pipe(
      map(model => createModelSuccess({ model })),
      catchError(error => of(createModelFail({ error }))),
    )),
  );

  @Effect()
  updateModel$ = this.actions.pipe(
    ofType(updateModel),
    switchMap(action => this.modelService.update(action.organizationId, action.applicationId, action.modelId, action.name).pipe(
      map(model => updateModelSuccess({ model })),
      catchError(error => of(updateModelFail({ error }))),
    )),
  );

  @Effect()
  deleteModel$ = this.actions.pipe(
    ofType(deleteModel),
    switchMap(action => this.modelService.delete(action.organizationId, action.applicationId, action.modelId).pipe(
      switchMap(() => [deleteModelSuccess(), setSelectedIdModel({ selectedId: null })]),
      catchError(error => of(deleteModelFail({ error }))),
    )),
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(loadModelsFail, createModelFail, updateModelFail, deleteModelFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );
}

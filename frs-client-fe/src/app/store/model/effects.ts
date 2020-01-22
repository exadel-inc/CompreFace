import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { ModelService } from 'src/app/core/model/model.service';
import { loadModelsEntityAction, addModelsEntityAction, createModelEntityAction } from './actions';
import { switchMap, map } from 'rxjs/operators';
import { forkJoin, of } from 'rxjs';

@Injectable()
export class ModelEffects {
  constructor(private actions: Actions, private modelService: ModelService) {}

  @Effect()
  fetchModels = this.actions.pipe(
    ofType(loadModelsEntityAction),
    switchMap(action => this.modelService.getAll(action.organizationId, action.applicationId)),
    map(models => addModelsEntityAction({ models }))
  );

  @Effect()
  createModel = this.actions.pipe(
    ofType(createModelEntityAction),
    switchMap(action => forkJoin([
      this.modelService.create(action.organizationId, action.applicationId, action.name),
      of(action)
    ])),
    map(ObservableRes => {
      const [model, { organizationId, applicationId }] = ObservableRes;

      return loadModelsEntityAction({ organizationId, applicationId });
    })
  );
}
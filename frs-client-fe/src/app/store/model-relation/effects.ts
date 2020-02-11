import {Injectable} from '@angular/core';
import {Effect, Actions, ofType} from '@ngrx/effects';
import {loadModelRelation, addModelRelation, putUpdatedModelRelation} from './actions';
import {switchMap, map} from 'rxjs/operators';
import {ModelRelationService} from 'src/app/core/model-relation/model-relation.service';
import {forkJoin, of} from 'rxjs';

@Injectable()
export class ModelRelationEffects {
  constructor(
    private actions: Actions,
    private modelRelationService: ModelRelationService
  ) { }

  @Effect()
  loadModelRelation = this.actions.pipe(
    ofType(loadModelRelation),
    switchMap(action => this.modelRelationService.getAll(action.organizationId, action.applicationId, action.modelId)),
    map(applications => addModelRelation({ applications }))
  );

  @Effect()
  updateModelRelationRole = this.actions.pipe(
    ofType(putUpdatedModelRelation),
    switchMap(action => forkJoin([
      this.modelRelationService.update(action.organizationId, action.applicationId, action.modelId, action.id, action.shareMode),
      of(action)
    ])),
    map(([application, { organizationId, applicationId, modelId }]) =>
      loadModelRelation({
        organizationId,
        applicationId,
        modelId
      }))
  );
}

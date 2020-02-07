import {createAction, props} from '@ngrx/store';
import {ModelRelation} from 'src/app/data/modelRelation';

interface ILoad {
  organizationId: string;
  applicationId: string;
  modelId: string;
}

interface IUpdate {
  organizationId: string;
  applicationId: string;
  modelId: string;
  id: string;
  shareMode: string;
}

export const loadModelRelation = createAction('[Model Relation/API] Load Model Relation', props<ILoad>());
export const addModelRelation = createAction('[Model Relation/API] Add Model Relation', props<{ applications: ModelRelation[] }>());
export const putUpdatedModelRelation = createAction('[Model Relation/API] Put Updated Model Relation', props<IUpdate>());
export const updateModelRelation = createAction('[Model Relation/API] Update Model Relation', props<{ application: ModelRelation }>());

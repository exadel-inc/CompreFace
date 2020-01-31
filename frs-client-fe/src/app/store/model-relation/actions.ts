import { createAction, props } from '@ngrx/store';
import { ModelRelation } from 'src/app/data/modelRelation';


export const loadModelRelation = createAction('[Model Relation/API] Load Model Relation', props<{ organizationId: string; applicationId: string; modelId: string }>());
export const addModelRelation = createAction('[Model Relation/API] Add Model Relation', props<{ applications: ModelRelation[] }>());
export const putUpdatedModelRelation = createAction('[Model Relation/API] Put Updated Model Relation', props<{
  organizationId: string;
  applicationId: string;
  modelId: string;
  id: string;
  role: string;
}>());
export const updateModelRelation = createAction('[Model Relation/API] Update Model Relation', props<{ application: ModelRelation }>());

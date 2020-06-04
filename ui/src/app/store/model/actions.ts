import { createAction, props } from '@ngrx/store';
import { Model, ModelUpdate } from 'src/app/data/model';

export const loadModels = createAction('[Model] Load Models', props<{ organizationId: string, applicationId: string }>());
export const loadModelsSuccess = createAction('[Model] Load Models Success', props<{ models: Model[] }>());
export const loadModelsFail = createAction('[Model] Load Models Fail', props<{ error: any }>());

export const createModel = createAction('[Model] Create Model', props<Partial<ModelUpdate>>());
export const createModelSuccess = createAction('[Model] Create Model Success', props<{ model: Model }>());
export const createModelFail = createAction('[Model] Create Model Fail', props<{ error: any }>());

export const updateModel = createAction('[Model] Update Model', props<ModelUpdate>());
export const updateModelSuccess = createAction('[Model] Update Model Success', props<{ model: Model }>());
export const updateModelFail = createAction('[Model] Update Model Fail', props<{ error: any }>());

export const deleteModel = createAction('[Model] Delete Model', props<Partial<ModelUpdate>>());
export const deleteModelSuccess = createAction('[Model] Delete Model Success', props<{ modelId: string }>());
export const deleteModelFail = createAction('[Model] Delete Model Fail', props<{ error: any }>());

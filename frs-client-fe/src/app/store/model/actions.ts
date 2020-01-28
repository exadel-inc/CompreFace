import { createAction, props } from '@ngrx/store';
import { Model } from 'src/app/data/model';

export const loadModelsEntityAction = createAction('[Model/API] Load Models', props<{ organizationId: string, applicationId: string }>());
export const addModelsEntityAction = createAction('[Model/API] Add Models', props<{ models: Model[] }>());
export const createModelEntityAction = createAction('[Model/API] Create Model', props<{ organizationId: string, applicationId: string, name: string }>());
export const setSelectedIdModelEntityAction = createAction('[Model/API] Set Id Model', props<{ selectedId: string}>());

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

import { createAction, props } from '@ngrx/store';
import { Model, ModelUpdate } from 'src/app/data/interfaces/model';

export const loadModel = createAction('[Model] Load Model', props<{ applicationId: string; selectedModelId: string }>());
export const loadModelSuccess = createAction('[Model] Load Model Success', props<{ model: Model }>());
export const loadModelFail = createAction('[Model] Load Model Fail', props<{ error: any }>());

export const loadModels = createAction('[Model] Load Models', props<{ applicationId: string }>());
export const loadModelsSuccess = createAction('[Model] Load Models Success', props<{ models: Model[] }>());
export const loadModelsFail = createAction('[Model] Load Models Fail', props<{ error: any }>());

export const createModel = createAction('[Model] Create Model', props<{ model: Partial<ModelUpdate> }>());
export const createModelSuccess = createAction('[Model] Create Model Success', props<{ model: Model }>());
export const createModelFail = createAction('[Model] Create Model Fail', props<{ error: any }>());

export const updateModel = createAction('[Model] Update Model', props<Partial<ModelUpdate>>());
export const updateModelSuccess = createAction('[Model] Update Model Success', props<{ model: Model }>());
export const updateModelFail = createAction('[Model] Update Model Fail', props<{ error: any }>());

export const cloneModel = createAction('[Model] Clone Model', props<Partial<ModelUpdate>>());
export const cloneModelSuccess = createAction('[Model] Clone Model Success', props<{ model: Model }>());
export const cloneModelFail = createAction('[Model] Clone Model Fail', props<{ error: any }>());

export const deleteModel = createAction('[Model] Delete Model', props<Partial<ModelUpdate>>());
export const deleteModelSuccess = createAction('[Model] Delete Model Success', props<{ modelId: string }>());
export const deleteModelFail = createAction('[Model] Delete Model Fail', props<{ error: any }>());

export const setSelectedModelIdEntityAction = createAction('[Model] Set Selected Model Id', props<{ selectedModelId }>());

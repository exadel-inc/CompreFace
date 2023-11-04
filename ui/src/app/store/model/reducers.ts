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
import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { Action, ActionReducer, createReducer, on } from '@ngrx/store';
import { Model } from 'src/app/data/interfaces/model';

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
  setSelectedModelIdEntityAction,
  updateModel,
  updateModelFail,
  updateModelSuccess,
  loadModelFail,
  loadModelSuccess,
} from './action';

export interface ModelEntityState extends EntityState<Model> {
  isPending: boolean;
  selectedModelId: string | null;
}

export const modelAdapter: EntityAdapter<Model> = createEntityAdapter<Model>();

const initialState: ModelEntityState = modelAdapter.getInitialState({
  isPending: false,
  selectedModelId: null,
});

const reducer: ActionReducer<ModelEntityState> = createReducer(
  initialState,
  on(loadModels, createModel, cloneModel, updateModel, deleteModel, state => ({ ...state, isPending: true })),
  on(loadModelFail, loadModelsFail, createModelFail, cloneModelFail, updateModelFail, deleteModelFail, state => ({
    ...state,
    isPending: false,
  })),
  on(loadModelsSuccess, (state, { models }) => modelAdapter.setAll(models, { ...state, isPending: false })),
  on(loadModelSuccess, (state, { model }) => modelAdapter.upsertOne(model, state)),
  on(createModelSuccess, cloneModelSuccess, (state, { model }) => modelAdapter.addOne(model, { ...state, isPending: false })),
  on(updateModelSuccess, (state, { model }) => modelAdapter.updateOne({ id: model.id, changes: model }, { ...state, isPending: false })),
  on(deleteModelSuccess, (state, { modelId }) => modelAdapter.removeOne(modelId, { ...state, isPending: false })),
  on(setSelectedModelIdEntityAction, (state, { selectedModelId }) => ({ ...state, selectedModelId }))
);

export const modelReducer = (modelState: ModelEntityState, action: Action) => reducer(modelState, action);

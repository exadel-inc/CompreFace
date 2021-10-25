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

import { EntityState } from '@ngrx/entity';
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { Model } from 'src/app/data/interfaces/model';

import { modelAdapter, ModelEntityState } from './reducers';
import { Role } from '../../data/enums/role.enum';
import { selectUserRollForSelectedApp } from '../application/selectors';
import { selectCurrentUserRole } from '../user/selectors';

export const selectModelEntityState = createFeatureSelector<EntityState<Model>>('model');
const { selectAll } = modelAdapter.getSelectors();

export const selectModels = createSelector(selectModelEntityState, selectAll);

export const selectPendingModel = createSelector(selectModelEntityState, (state: ModelEntityState) => state.isPending);

export const selectCurrentModelId = createSelector(selectModelEntityState, (state: ModelEntityState) => state.selectedModelId);

export const selectCurrentModel = createSelector(selectModelEntityState, selectCurrentModelId, (models, selectedModelId) =>
  models.entities ? models.entities[selectedModelId] : null
);

export const selectCurrentApiKey = createSelector(
  selectModelEntityState,
  selectCurrentModel,
  (models, selectedModel) => selectedModel?.apiKey
);

export const selectUserRole = createSelector(selectUserRollForSelectedApp, selectCurrentUserRole, (applicationRole, globalRole) =>
  globalRole !== Role.User ? globalRole : applicationRole
);

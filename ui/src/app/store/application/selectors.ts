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

import { Application } from '../../data/interfaces/application';
import { AppEntityState, selectAll, selectEntities } from './reducers';

export const selectApplicationEntityState = createFeatureSelector<EntityState<Application>>('application');

export const selectApplicationById = (id: string) =>
  createSelector(selectApplicationEntityState, selectEntities, appsDictionary => appsDictionary[id]);

export const selectApplications = createSelector(selectApplicationEntityState, selectAll);

export const selectCurrentAppId = createSelector(selectApplicationEntityState, (state: AppEntityState) => state.selectedAppId);

export const selectCurrentApp = createSelector(selectApplicationEntityState, selectCurrentAppId, (apps, selectedId) =>
  apps.entities ? apps.entities[selectedId] : null
);

export const selectUserRollForSelectedApp = createSelector(selectCurrentApp, app => (app ? app.role : null));

export const selectIsPendingApplicationList = createSelector(selectApplicationEntityState, (state: AppEntityState) => state.isPending);

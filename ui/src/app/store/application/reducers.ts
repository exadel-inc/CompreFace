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

import { Application } from '../../data/interfaces/application';
import {
  createApplication,
  createApplicationFail,
  createApplicationSuccess,
  deleteApplication,
  deleteApplicationFail,
  deleteApplicationSuccess,
  loadApplications,
  loadApplicationsFail,
  loadApplicationsSuccess,
  setSelectedAppIdEntityAction,
  updateApplication,
  updateApplicationFail,
  updateApplicationSuccess,
  refreshApplication,
} from './action';

export const applicationAdapter: EntityAdapter<Application> = createEntityAdapter<Application>();
export const { selectEntities, selectAll } = applicationAdapter.getSelectors();

export interface AppEntityState extends EntityState<Application> {
  // additional entities state properties
  selectedAppId: string | null;
  isPending: boolean;
}

export const initialState: AppEntityState = applicationAdapter.getInitialState({
  // additional entity state properties
  selectedAppId: null,
  isPending: false,
});

const reducer: ActionReducer<AppEntityState> = createReducer(
  initialState,
  on(loadApplications, createApplication, updateApplication, deleteApplication, state => ({ ...state, isPending: true })),
  on(loadApplicationsFail, createApplicationFail, updateApplicationFail, deleteApplicationFail, state => ({ ...state, isPending: false })),
  on(createApplicationSuccess, (state, { application }) => applicationAdapter.addOne(application, { ...state, isPending: false })),
  on(loadApplicationsSuccess, (state, { applications }) => applicationAdapter.setAll(applications, { ...state, isPending: false })),
  on(updateApplicationSuccess, (state, { application }) =>
    applicationAdapter.updateOne({ id: application.id, changes: application }, { ...state, isPending: false })
  ),
  on(deleteApplicationSuccess, (state, { id }) => applicationAdapter.removeOne(id, { ...state, isPending: false })),
  on(setSelectedAppIdEntityAction, (state, { selectedAppId }) => ({ ...state, selectedAppId })),
  on(refreshApplication, (state, { userId, lastName, firstName }) => {
    const appsToUpdate = selectAll(state)
      .filter(app => app.owner.userId === userId)
      .map(app => ({ id: app.id, changes: { ...app, owner: { userId, firstName, lastName } } }));
    return applicationAdapter.updateMany(appsToUpdate, { ...state, isPending: false });
  })
);

export const applicationReducer = (appState: AppEntityState, action: Action) => reducer(appState, action);

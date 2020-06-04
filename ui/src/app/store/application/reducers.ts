import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { Action, ActionReducer, createReducer, on } from '@ngrx/store';
import { Application } from 'src/app/data/application';

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
  setSelectedIdEntityAction,
  updateApplication,
  updateApplicationFail,
  updateApplicationSuccess,
} from './action';

export const applicationAdapter: EntityAdapter<Application> = createEntityAdapter<Application>();

export interface AppEntityState extends EntityState<Application> {
  // additional entities state properties
  selectedAppId: string | null;
  isPending: boolean;
}

export const initialState: AppEntityState = applicationAdapter.getInitialState({
  // additional entity state properties
  selectedAppId: null,
  isPending: false
});

const reducer: ActionReducer<AppEntityState> = createReducer(
  initialState,
  on(loadApplications, createApplication, updateApplication, deleteApplication, (state) => ({ ...state, isPending: true })),
  on(loadApplicationsFail, createApplicationFail, updateApplicationFail, deleteApplicationFail,
    (state) => ({ ...state, isPending: false })),
  on(createApplicationSuccess, (state, { application }) => applicationAdapter.addOne(application, { ...state, isPending: false })),
  on(loadApplicationsSuccess, (state, { applications }) => applicationAdapter.addAll(applications, { ...state, isPending: false })),
  on(updateApplicationSuccess, (state, { application }) => applicationAdapter.updateOne(
    { id: application.id, changes: application },
    { ...state, isPending: false }
  )),
  on(deleteApplicationSuccess, (state, { id }) => applicationAdapter.removeOne(id, { ...state, isPending: false })),
  on(setSelectedIdEntityAction, (state, { selectedAppId }) => ({ ...state, selectedAppId }))
);

export function ApplicationReducer(appState: AppEntityState, action: Action) {
  return reducer(appState, action);
}

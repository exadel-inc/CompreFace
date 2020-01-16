import { Application } from 'src/app/data/application';
import {addApplication, addApplications, setSelectedId} from './action';
import { EntityState, createEntityAdapter, EntityAdapter } from '@ngrx/entity';
import {Action, createReducer, on} from "@ngrx/store";

export const applicationAdapter: EntityAdapter<Application> = createEntityAdapter<Application>();

export interface AppEntityState extends EntityState<Application> {
  // additional entities state properties
  selectedAppId: number | null;
}

export const initialState: AppEntityState = applicationAdapter.getInitialState({
  // additional entity state properties
  selectedAppId: null,
});

const reducer = createReducer(
  initialState,
  on(addApplication, (state, { application }) => {
    return applicationAdapter.addOne(application, state);
  }),
  on(addApplications, (state, { applications }) => {
    return applicationAdapter.addAll(applications, state);
  }),
  on(setSelectedId, (state, { selectedAppId }) => {
    return { ...state, selectedAppId };
  })
);

export function ApplicationReducer(state = initialState, action: Action): AppEntityState {
  return reducer(state, action);
}

export const getSelectedAppId = (state: AppEntityState) => state.selectedAppId;

const {
  selectIds,
  selectEntities,
  selectAll,
  selectTotal,
} = applicationAdapter.getSelectors();

// select the array of applications ids
export const selectAppIds = selectIds;

// select the dictionary of application entities
export const selectAppEntities = selectEntities;

// select the array of applications
export const selectAllApps = selectAll;

// select the total application count
export const selectAppTotal = selectTotal;

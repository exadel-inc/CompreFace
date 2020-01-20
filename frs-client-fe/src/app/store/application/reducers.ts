import { Application } from 'src/app/data/application';
import {addApplication, addApplications, setSelectedId, updateApplication} from './action';
import { EntityState, createEntityAdapter, EntityAdapter } from '@ngrx/entity';
import {ActionReducer, createReducer, on} from "@ngrx/store";

export const applicationAdapter: EntityAdapter<Application> = createEntityAdapter<Application>();

export interface AppEntityState extends EntityState<Application> {
  // additional entities state properties
  selectedAppId: string | null;
}

export const initialState: AppEntityState = applicationAdapter.getInitialState({
  // additional entity state properties
  selectedAppId: null,
});

export const ApplicationReducer:ActionReducer<AppEntityState> = createReducer(
  initialState,
  on(addApplication, (state, { application }) => {
    return applicationAdapter.addOne(application, state);
  }),
  on(addApplications, (state, { applications }) => {
    return applicationAdapter.addAll(applications, state);
  }),
  on(updateApplication, (state, { application }) => {
    return applicationAdapter.updateOne({ id: application.id, changes: application }, state);
  }),
  on(setSelectedId, (state, { selectedAppId }) => {
    return { ...state, selectedAppId };
  })
);

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

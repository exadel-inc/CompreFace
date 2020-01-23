import { Application } from 'src/app/data/application';
import {
  addApplicationEntityAction,
  addApplicationsEntityAction,
  loadApplicationsEntityAction,
  setSelectedIdEntityAction,
  updateApplicationEntityAction,
  putUpdatedApplicationEntityAction
} from './action';
import { EntityState, createEntityAdapter, EntityAdapter } from '@ngrx/entity';
import {ActionReducer, createReducer, on} from "@ngrx/store";

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

export const ApplicationReducer:ActionReducer<AppEntityState> = createReducer(
  initialState,
  on(loadApplicationsEntityAction, (state) => ({
    ...state,
    isPending: true
  })),
  on(addApplicationEntityAction, (state, { application }) => {
    const newState = {
      ...state,
      isPending: false
    };

    return applicationAdapter.addOne(application, newState);
  }),
  on(addApplicationsEntityAction, (state, { applications }) => {
    const newState = {
      ...state,
      isPending: false
    };

    return applicationAdapter.addAll(applications, newState);
  }),
  on(putUpdatedApplicationEntityAction, (state) => ({
    ...state,
    isPending: true
  })),
  on(updateApplicationEntityAction, (state, { application }) => {
    const newState = {
      ...state,
      isPending: false
    };

    return applicationAdapter.updateOne({ id: application.id, changes: application }, newState);
  }),
  on(setSelectedIdEntityAction, (state, { selectedAppId }) => {
    return { ...state, selectedAppId };
  })
);

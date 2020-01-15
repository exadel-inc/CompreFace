import { Application } from 'src/app/data/application';
import { ApplicationEntityActionType, ApplicationEntityActionList } from './action';
import { EntityState, createEntityAdapter, EntityAdapter } from '@ngrx/entity';

export const applicationAdapter: EntityAdapter<Application> = createEntityAdapter<Application>();

export interface AppEntityState extends EntityState<Application> {
  // additional entities state properties
  selectedAppId: number | null;
}

export const initialState: AppEntityState = applicationAdapter.getInitialState({
  // additional entity state properties
  selectedAppId: null,
});

export function ApplicationReducer(state = initialState, action: ApplicationEntityActionType): AppEntityState {
  switch(action.type) {
    case ApplicationEntityActionList.ADD_APPLICATION: {
      return applicationAdapter.addOne(action.payload.application, state);
    }

    case ApplicationEntityActionList.ADD_APPLICATIONS: {
      return applicationAdapter.addAll(action.payload.applications, state);
    }

    default: {
      return state;
    }
  }
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

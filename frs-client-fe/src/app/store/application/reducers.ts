import { Application } from 'src/app/data/application';
import { ApplicationEntityActionType, ApplicationEntityActionList } from './action';
import { EntityState, createEntityAdapter, EntityAdapter } from '@ngrx/entity';

export const applicationAdapter: EntityAdapter<Application> = createEntityAdapter<Application>();

const initialState: EntityState<Application> = applicationAdapter.getInitialState();

export function ApplicationReducer(state = initialState, action: ApplicationEntityActionType): EntityState<Application> {
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

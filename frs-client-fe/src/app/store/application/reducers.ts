import {Application} from 'src/app/data/application';
import {
  addApplicationEntityAction,
  addApplicationsEntityAction,
  loadApplicationsEntityAction,
  setSelectedIdEntityAction,
  updateApplicationEntityAction,
  putUpdatedApplicationEntityAction
} from './action';
import {EntityState, createEntityAdapter, EntityAdapter} from '@ngrx/entity';
import {createReducer, on, Action} from '@ngrx/store';

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

export function ApplicationReducer(appState: AppEntityState, action: Action) {
  return createReducer(
    initialState,
    on(loadApplicationsEntityAction, (state) => ({ ...state, isPending: true })),
    on(addApplicationEntityAction, (state, { application }) => applicationAdapter.addOne(application, { ...state, isPending: false })),
    on(addApplicationsEntityAction, (state, { applications }) => applicationAdapter.addAll(applications, { ...state, isPending: false })),
    on(putUpdatedApplicationEntityAction, (state) => ({ ...state, isPending: true })),
    on(updateApplicationEntityAction, (state, { application }) => applicationAdapter.updateOne(
      { id: application.id, changes: application },
      { ...state, isPending: false }
    )),
    on(setSelectedIdEntityAction, (state, { selectedAppId }) => ({ ...state, selectedAppId }))
  )(appState, action);
}

import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Action, ActionReducer, createReducer, on } from '@ngrx/store';
import { AppUser } from 'src/app/data/appUser';

import {
  addAppUserEntityAction,
  deleteUserFromApplication,
  deleteUserFromApplicationFail,
  deleteUserFromApplicationSuccess,
  loadAppUserEntityAction,
  putUpdatedAppUserRoleEntityAction,
  updateUserRoleEntityAction,
} from './actions';

export interface AppUserEntityState extends EntityState<AppUser> {
  isPending: boolean;
}

export const appUserAdapter = createEntityAdapter<AppUser>();

const initialState: AppUserEntityState = appUserAdapter.getInitialState({
  isPending: false
});

const reducer: ActionReducer<AppUserEntityState> = createReducer(
  initialState,
  on(loadAppUserEntityAction, deleteUserFromApplication, (state) => ({ ...state, isPending: true })),
  on(deleteUserFromApplicationFail, (state) => ({ ...state, isPending: false })),
  on(addAppUserEntityAction, (state, { users }) => {
    const newState = { ...state, isPending: false };
    return appUserAdapter.addAll(users, newState);
  }),
  on(putUpdatedAppUserRoleEntityAction, (state) => ({ ...state, isPending: true })),
  on(updateUserRoleEntityAction, (state, { user }) => {
    const newState = { ...state, isPending: false };
    return appUserAdapter.updateOne({
      id: user.id,
      changes: {
        role: user.role
      }
    }, newState);
  }),
  on(deleteUserFromApplicationSuccess, (state, { id }) => appUserAdapter.removeOne(id, { ...state, isPending: false })),
);


export function appUserReducer(appUserState: AppUserEntityState, action: Action) {
  return reducer(appUserState, action);
}

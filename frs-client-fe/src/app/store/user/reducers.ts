import { createReducer, on, ActionReducer } from '@ngrx/store';
import { AppUser } from 'src/app/data/appUser';
import {
  SetPending,
  AddUsersEntityAction,
  UpdateUserRoleEntityAction,
  LoadUsersEntityAction,
  PutUpdatedUserRoleEntityAction
} from './action';

import { EntityState, createEntityAdapter, EntityAdapter } from '@ngrx/entity';

export interface AppUserEntityState extends EntityState<AppUser> {
  isPending: boolean;
}

export const userAdapter: EntityAdapter<AppUser> = createEntityAdapter<AppUser>();
const initialState: AppUserEntityState = userAdapter.getInitialState({
  isPending: false
});

export const AppUserReducer: ActionReducer<AppUserEntityState> = createReducer(
  initialState,
  on(LoadUsersEntityAction, (state) => ({
    ...state,
    isPending: true
  })),
  on(SetPending, (state, { isPending }) => ({
    ...state,
    isPending
  })),
  on(AddUsersEntityAction, (state, { users }) => {
    const newState = {
      ...state,
      isPending: false
    };

    return userAdapter.addAll(users, newState);
  }),
  on(PutUpdatedUserRoleEntityAction, (state) => ({
    ...state,
    isPending: true
  })),
  on(UpdateUserRoleEntityAction, (state, { user }) => {
    const newState = {
      ...state,
      isPending: false
    };

    return userAdapter.updateOne({
      id: user.id,
      changes: {
        accessLevel: user.accessLevel
      }
    }, newState)
  }));

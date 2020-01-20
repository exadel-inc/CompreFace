import { createReducer, on, ActionReducer } from '@ngrx/store';
import { AppUser } from 'src/app/data/appUser';
import { SetPending, AddUsersEntityAction, UpdateUserRoleEntityAction } from './action';

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
  on(SetPending, (state, { isPending }) => ({
    ...state,
    isPending
  })),
  on(AddUsersEntityAction, (state, { users }) => {
    return userAdapter.addAll(users, state);
  }),
  on(UpdateUserRoleEntityAction, (state, { user }) => {
    return userAdapter.updateOne({
      id: user.id,
      changes: {
        accessLevel: user.accessLevel
      }
    }, state)
  }));

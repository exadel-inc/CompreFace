/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import {createReducer, on, Action, ActionReducer} from '@ngrx/store';
import {AppUser} from 'src/app/data/appUser';
import {
  SetPending,
  AddUsersEntityAction,
  UpdateUserRoleEntityAction,
  LoadUsersEntityAction,
  PutUpdatedUserRoleEntityAction
} from './action';

import {EntityState, createEntityAdapter, EntityAdapter} from '@ngrx/entity';

export interface AppUserEntityState extends EntityState<AppUser> {
  isPending: boolean;
}

export const userAdapter: EntityAdapter<AppUser> = createEntityAdapter<AppUser>();
const initialState: AppUserEntityState = userAdapter.getInitialState({
  isPending: false
});

const reducer: ActionReducer<AppUserEntityState> = createReducer(
  initialState,
  on(LoadUsersEntityAction, (state) => ({...state, isPending: true})),
  on(SetPending, (state, {isPending}) => ({...state, isPending})),
  on(AddUsersEntityAction, (state, {users}) => {
    return userAdapter.addAll(users, {...state, isPending: false});
  }),
  on(PutUpdatedUserRoleEntityAction, (state) => ({...state, isPending: true})),
  on(UpdateUserRoleEntityAction, (state, {user}) => {
    return userAdapter.updateOne({
      id: user.id,
      changes: {
        role: user.role
      }
    }, {...state, isPending: false});
  })
);

export function AppUserReducer(appUserState: AppUserEntityState, action: Action) {
  return reducer(appUserState, action);
}

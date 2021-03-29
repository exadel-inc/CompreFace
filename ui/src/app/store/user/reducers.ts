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
import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { Action, ActionReducer, createReducer, on } from '@ngrx/store';
import { AppUser } from 'src/app/data/interfaces/app-user';

import {
  addUsersEntity,
  loadUsersEntity,
  setPending,
  updateUserRole,
  updateUserRoleFail,
  updateUserRoleSuccess,
  refreshUserName,
} from './action';

export interface AppUserEntityState extends EntityState<AppUser> {
  isPending: boolean;
}

export const userAdapter: EntityAdapter<AppUser> = createEntityAdapter<AppUser>();
const initialState: AppUserEntityState = userAdapter.getInitialState({
  isPending: false,
});

const reducer: ActionReducer<AppUserEntityState> = createReducer(
  initialState,
  on(loadUsersEntity, updateUserRole, state => ({ ...state, isPending: true })),
  on(setPending, (state, { isPending }) => ({ ...state, isPending })),
  on(addUsersEntity, (state, { users }) => userAdapter.setAll(users, { ...state, isPending: false })),
  on(updateUserRoleSuccess, (state, { user }) =>
    userAdapter.updateOne(
      {
        id: user.userId,
        changes: {
          role: user.role,
        },
      },
      { ...state, isPending: false }
    )
  ),
  on(updateUserRoleFail, state => ({ ...state, isPending: false })),
  on(refreshUserName, (state, { userId, firstName, lastName }) =>
    userAdapter.updateOne({ id: userId, changes: { firstName, lastName } }, { ...state })
  )
);

export const appUserReducer = (appUserState: AppUserEntityState, action: Action) => reducer(appUserState, action);

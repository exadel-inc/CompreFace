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
import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Action, ActionReducer, createReducer, on } from '@ngrx/store';
import { AppUser } from 'src/app/data/interfaces/app-user';

import {
  addAppUserEntityAction,
  deleteUserFromApplication,
  deleteUserFromApplicationFail,
  deleteUserFromApplicationSuccess,
  loadAppUserEntityAction,
  updateAppUserRole,
  updateAppUserRoleFail,
  updateAppUserRoleSuccess,
} from './action';

export interface AppUserEntityState extends EntityState<AppUser> {
  isPending: boolean;
}

export const appUserAdapter = createEntityAdapter<AppUser>();

const initialState: AppUserEntityState = appUserAdapter.getInitialState({
  isPending: false,
});

const reducer: ActionReducer<AppUserEntityState> = createReducer(
  initialState,
  on(loadAppUserEntityAction, deleteUserFromApplication, updateAppUserRole, state => ({ ...state, isPending: true })),
  on(deleteUserFromApplicationFail, updateAppUserRoleFail, state => ({ ...state, isPending: false })),
  on(addAppUserEntityAction, (state, { users }) => {
    const newState = { ...state, isPending: false };
    return appUserAdapter.setAll(users, newState);
  }),
  on(updateAppUserRoleSuccess, (state, { user }) =>
    appUserAdapter.updateOne(
      {
        id: user.userId,
        changes: {
          role: user.role,
        },
      },
      { ...state, isPending: false }
    )
  ),
  on(deleteUserFromApplicationSuccess, (state, { id }) => appUserAdapter.removeOne(id, { ...state, isPending: false }))
);

export const appUserReducer = (appUserState: AppUserEntityState, action: Action) => reducer(appUserState, action);

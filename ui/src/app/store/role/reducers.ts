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

import {Role} from 'src/app/data/role';
import {createEntityAdapter, EntityAdapter, EntityState} from '@ngrx/entity';
import {SetPendingRoleEntityAction, FetchRolesEntityAction, LoadRolesEntityAction} from './actions';
import {createReducer, on, Action, ActionReducer} from '@ngrx/store';

export interface RoleEntityState extends EntityState<Role> {
  isPending: boolean;
}

const roleAdapter: EntityAdapter<Role> = createEntityAdapter<Role>();
export const initialState: RoleEntityState = roleAdapter.getInitialState({
  isPending: false
});

const reducer: ActionReducer<RoleEntityState> = createReducer(
  initialState,
  on(LoadRolesEntityAction, (state) => ({ ...state, isPending: true })),
  on(SetPendingRoleEntityAction, (state, { isPending }) => ({ ...state, isPending })),
  on(FetchRolesEntityAction, (state, { role }) => {
    const newState = roleAdapter.removeAll(state);
    newState.isPending = false;
    return roleAdapter.addOne({ id: 0, accessLevels: role.accessLevels }, newState);
  })
);

export function RoleReducer(roleState: RoleEntityState, action: Action) {
  return reducer(roleState, action);
}

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
import { Role } from 'src/app/data/interfaces/role';

import { fetchRolesEntity, loadRolesEntity, setPendingRoleEntity } from './action';

export interface RoleEntityState extends EntityState<Role> {
  isPending: boolean;
}

const roleAdapter: EntityAdapter<Role> = createEntityAdapter<Role>();
export const initialState: RoleEntityState = roleAdapter.getInitialState({
  isPending: false,
});

const reducer: ActionReducer<RoleEntityState> = createReducer(
  initialState,
  on(loadRolesEntity, state => ({ ...state, isPending: true })),
  on(setPendingRoleEntity, (state, { isPending }) => ({ ...state, isPending })),
  on(fetchRolesEntity, (state, { role }) => roleAdapter.setOne({ id: 0, accessLevels: role.accessLevels }, { ...state, isPending: false }))
);

export const roleReducer = (roleState: RoleEntityState, action: Action) => reducer(roleState, action);

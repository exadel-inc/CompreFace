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

import {createAction, props} from '@ngrx/store';
import {AppUser} from 'src/app/data/appUser';

export const SetPending = createAction('[User/API] Set Pending', props<{ isPending: boolean }>());
export const LoadUsersEntityAction = createAction('[User/API] Load Users', props<{ organizationId: string }>());
export const AddUsersEntityAction = createAction('[User/API] Add Users', props<{ users: AppUser[] }>());
export const PutUpdatedUserRoleEntityAction = createAction(
  '[User/API] Put Updated User Role',
  props<{ organizationId: string; user: { id: string, role: string } }>()
);
export const UpdateUserRoleEntityAction = createAction('[User/API] Update Role', props<{ user: AppUser }>());
export const DeleteUserFromOrganization = createAction('[User/API] Delete User', props<{ userId: string; organizationId: string }>());

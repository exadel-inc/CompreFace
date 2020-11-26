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

import { createAction, props } from '@ngrx/store';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { Role } from 'src/app/data/enums/role.enum';

export const loadAppUserEntityAction = createAction(
  '[App-User/API] Load App Users',
  props<{
    applicationId: string;
  }>()
);
export const addAppUserEntityAction = createAction('[App-User/API] Add App Users', props<{ users: AppUser[] }>());
export const addAppUserEntityActionSuccess = createAction('[App-User/API] Add User To Application Success', props<{ userEmail: string }>());
export const addAppUserEntityActionFail = createAction('[App-User/API] Add User To Application Fail', props<{ error: any }>());
export const updateAppUserRoleAction = createAction(
  '[App-User/API] Update App User',
  props<{ applicationId: string; user: { id: string; role: Role } }>()
);
export const updateAppUserRoleSuccessAction = createAction('[App-User/API] Update App User Role Success', props<{ user: AppUser }>());
export const updateAppUserRoleFailAction = createAction('[App-User/API] Update App User Role Failed)', props<{ error: any }>());
export const deleteUserFromApplication = createAction(
  '[App-User] Delete User From Application',
  props<{ userId: string; applicationId: string }>()
);
export const deleteUserFromApplicationSuccess = createAction('[App-User] Delete User From Application Success', props<{ id: string }>());
export const deleteUserFromApplicationFail = createAction('[App-User] Delete User From Application Fail', props<{ error: any }>());

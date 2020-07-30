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
import { AppUser } from 'src/app/data/appUser';
import { RoleEnum } from 'src/app/data/roleEnum.enum';

export const loadAppUserEntityAction = createAction('[App-User/API] Load App Users', props<{
  organizationId: string,
  applicationId: string
}>());
export const addAppUserEntityAction = createAction('[App-User/API] Add App Users', props<{ users: AppUser[] }>());
export const putUpdatedAppUserRoleEntityAction = createAction(
  '[App-User/API] Put Updated App User',
  props<{ organizationId: string; applicationId: string; user: { id: string, role: RoleEnum } }>()
);
export const updateUserRoleEntityAction = createAction('[App-User/API] Update App User Role', props<{ user: AppUser }>());

export const deleteUserFromApplication = createAction('[App-User] Delete User From Application',
  props<{ userId: string; organizationId: string, applicationId: string }>());
export const deleteUserFromApplicationSuccess = createAction('[App-User] Delete User From Application Success', props<{ id: string }>());
export const deleteUserFromApplicationFail = createAction('[App-User] Delete User From Application Fail', props<{ error: any }>());

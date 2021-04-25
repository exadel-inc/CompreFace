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
import { DeleteUserParams } from '../../data/interfaces/delete-user';
import { UpdateUserRole } from '../../data/interfaces/update-user-role';
import { UserInfo } from '../../data/interfaces/user-info';

export const setPending = createAction('[User/API] Set Pending', props<{ isPending: boolean }>());
export const loadUsersEntity = createAction('[User/API] Load Users');
export const addUsersEntity = createAction('[User/API] Add Users', props<{ users: AppUser[] }>());

export const updateUserRole = createAction('[User/API] Update User Role', props<UpdateUserRole>());
export const updateUserRoleWithRefresh = createAction('[User/API] Update User Role With Refresh', props<UpdateUserRole>());
export const updateUserRoleSuccess = createAction('[User/API] Update User Role Success', props<{ user: AppUser }>());
export const updateUserRoleFail = createAction('[User/API] Update User Role Failed)', props<{ error: any }>());

export const deleteUser = createAction('[User/API] Delete User', props<DeleteUserParams>());
export const deleteUserSuccess = createAction('[User/API] Delete User Success', props<{ userId: string }>());
export const deleteUserFail = createAction('[User/API] Delete User Fail', props<{ error: any }>());

export const refreshUserName = createAction('[User] Refresh User Name', props<UserInfo>());

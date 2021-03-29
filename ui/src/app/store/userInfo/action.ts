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

import { User } from '../../data/interfaces/user';
import { EditUserInfo } from '../../data/interfaces/edit-user-info';
import { UserInfo } from '../../data/interfaces/user-info';

export const updateUserInfo = createAction('[User] Login Success', props<{ isAuthenticated?: boolean }>());
export const resetUserInfo = createAction('[User] Update Token');

export const getUserInfo = createAction('[User] Get User Info');
export const getUserInfoSuccess = createAction('[User] Get User Info Success', props<{ user: User }>());
export const getUserInfoFail = createAction('[User] Get User Info Fail', props<{ error: any }>());

export const editUserInfo = createAction('[User/API] Update User Info', props<EditUserInfo>());
export const editUserInfoSuccess = createAction('[User/API] Update User Info Success', props<UserInfo>());
export const editUserInfoFail = createAction('[User/API] Update User Info Fail', props<{ error: any }>());

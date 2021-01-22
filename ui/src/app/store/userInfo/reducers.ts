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
import { Action, ActionReducer, createReducer, on } from '@ngrx/store';

import { User } from '../../data/interfaces/user';
import { getUserInfoSuccess, resetUserInfo, updateUserInfo, editUserInfoSuccess } from './action';

export const initialState: User = {
  guid: null,
  email: null,
  password: null,
  firstName: null,
  lastName: null,
  avatar: null,
  userId: null,
};

const reducer: ActionReducer<User> = createReducer(
  initialState,
  on(updateUserInfo, (state, action) => ({ ...state, ...action })),
  on(resetUserInfo, () => ({ ...initialState })),
  on(getUserInfoSuccess, (state, action) => ({ ...state, ...action.user })),
  on(editUserInfoSuccess, (state, userInfo) => ({ ...state, ...userInfo }))
);

export const userInfoReducer = (userInfoState: User, action: Action) => reducer(userInfoState, action);

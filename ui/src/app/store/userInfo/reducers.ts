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

import { createReducer, on, Action, ActionReducer } from '@ngrx/store';
import { updateUserInfo, resetUserInfo, updateUserAuthorization, getUserInfoSuccess } from './action';
import { User } from '../../data/interfaces/user';

export interface UserInfoState extends User {
  isAuthenticated: boolean;
}

export const initialState: UserInfoState = {
  isAuthenticated: false,
  guid: null,
  email: null,
  password: null,
  firstName: null,
  lastName: null,
  avatar: null,
  userId: null,
};

const reducer: ActionReducer<UserInfoState> = createReducer(initialState,
  on(updateUserInfo, (state, action) => ({ ...state, ...action })),
  on(resetUserInfo, () => ({ ...initialState })),
  on(updateUserAuthorization, (state, action) => ({ ...state, isAuthenticated: action.value })),
  on(getUserInfoSuccess, (state, action) => ({ ...state, ...action.user })),
);

export function UserInfoReducer(userInfoState: UserInfoState, action: Action) {
  return reducer(userInfoState, action);
}

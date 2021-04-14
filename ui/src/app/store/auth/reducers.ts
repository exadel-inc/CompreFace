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
import {
  logIn,
  logInSuccess,
  logInFail,
  signUpSuccess,
  signUpFail,
  logOut,
  resetErrorMessage,
  changePassword,
  changePasswordSuccess,
  changePasswordFail,
  signUp,
} from './action';

export interface AuthState {
  isLoading: boolean;
}

export const initialState: AuthState = {
  isLoading: false,
};

const reducer: ActionReducer<AuthState> = createReducer(
  initialState,

  on(logIn, signUp, changePassword, state => ({
    ...state,
    isLoading: true,
  })),
  on(logInSuccess, logInFail, signUpFail, changePasswordSuccess, signUpSuccess, changePasswordFail, state => ({
    ...state,
    isLoading: false,
  })),
  on(resetErrorMessage, state => ({
    ...state,
  })),
  on(logOut, () => ({ ...initialState }))
);

export const authReducer = (authState: AuthState, action: Action) => reducer(authState, action);

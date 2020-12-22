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
import { logInSuccess, logInFailure, signUpSuccess, signUpFailure, logOut, resetErrorMessage } from './action';

export interface AuthState {
  errorMessage: string | null;
  successMessage: string | null;
  isLoading: boolean;
}

export const initialState: AuthState = {
  errorMessage: null,
  successMessage: null,
  isLoading: false,
};

const reducer: ActionReducer<AuthState> = createReducer(
  initialState,
  on(logInSuccess, state => ({
    ...state,
    errorMessage: null,
    isLoading: false,
  })),
  on(logInFailure, state => ({
    ...state,
    errorMessage: 'E-mail or Password is incorrect.',
    isLoading: false,
  })),
  on(signUpSuccess, state => ({
    ...state,
    errorMessage: null,
    successMessage: 'You have created new account, please login into your account',
    isLoading: false,
  })),
  on(signUpFailure, state => ({
    ...state,
    errorMessage: 'This e-mail is already in use.',
    successMessage: null,
    isLoading: false,
  })),
  on(resetErrorMessage, state => ({
    ...state,
    errorMessage: null,
  })),
  on(logOut, () => ({ ...initialState }))
);

export const authReducer = (authState: AuthState, action: Action) => reducer(authState, action);

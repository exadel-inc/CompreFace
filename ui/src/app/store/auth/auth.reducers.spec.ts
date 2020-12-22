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
import { logInFailure, logInSuccess, signUpFailure, signUpSuccess } from './action';
import { authReducer, initialState } from './reducers';

describe('AuthReducer', () => {
  describe('LOGIN_SUCCESS action', () => {
    it('should set isAuthenticated to true, and loading false', () => {
      const action = logInSuccess();
      const state = authReducer(initialState, action);

      expect(state.errorMessage).toBe(null);
      expect(state.isLoading).toBe(false);
    });
  });

  describe('LOGIN_FAILURE action', () => {
    it('should set errorMessage to value, and loading false', () => {
      const action = logInFailure({ error: { error: {} } });
      const state = authReducer(initialState, action);

      expect(state.errorMessage).toBe('E-mail or Password is incorrect.');
      expect(state.isLoading).toBe(false);
    });
  });

  describe('SIGNUP_SUCCESS action', () => {
    it('should set successMessage to value, and loading false', () => {
      const action = signUpSuccess({ confirmationNeeded: false });
      const state = authReducer(initialState, action);

      expect(state.successMessage).toBe('You have created new account, please login into your account');
      expect(state.errorMessage).toBe(null);
      expect(state.isLoading).toBe(false);
    });
  });

  describe('SIGNUP_FAILURE action', () => {
    it('should set errorMessage to value, and loading false', () => {
      const action = signUpFailure({ error: {} });
      const state = authReducer(initialState, action);

      expect(state.errorMessage).toBe('This e-mail is already in use.');
      expect(state.successMessage).toBe(null);
      expect(state.isLoading).toBe(false);
    });
  });
});

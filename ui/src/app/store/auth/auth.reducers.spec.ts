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
import { TestBed } from '@angular/core/testing';
import { logInFail, logInSuccess, signUpFail, signUpSuccess, changePasswordSuccess, changePasswordFail, logIn, signUp } from './action';
import { authReducer, initialState } from './reducers';

describe('AuthReducer', () => {
  describe('LOGIN_SUCCESS action', () => {
    it('should set isAuthenticated to true, and loading false', () => {
      const action = logInSuccess();
      const state = authReducer(initialState, action);

      expect(state.isLoading).toBe(false);
    });
  });

  describe('LOGIN_FAILURE action', () => {
    it('should set loading to false', () => {
      const action = logInFail({ error: { error: {} } });
      const state = authReducer(initialState, action);
      expect(state.isLoading).toBe(false);
    });
  });

  describe('LOGIN action', () => {
    it('should set loading to true', () => {
      const action = logIn({ email: '', password: 'string' });
      const state = authReducer(initialState, action);
      expect(state.isLoading).toBe(true);
    });
  });

  describe('SIGNUP_SUCCESS action', () => {
    it('should loading false', () => {
      const action = signUpSuccess({ confirmationNeeded: false, email: 'test@test.com', password: 'test1234' });
      const state = authReducer(initialState, action);

      expect(state.isLoading).toBe(false);
    });
  });

  describe('SIGNUP_FAILURE action', () => {
    it('should set loading to false', () => {
      const action = signUpFail({ error: {} });
      const state = authReducer(initialState, action);

      expect(state.isLoading).toBe(false);
    });
  });

  describe('SIGNUP action', () => {
    it('should set loading to true', () => {
      const action = signUp({ email: '', firstName: '', lastName: '', password: '' });
      const state = authReducer(initialState, action);

      expect(state.isLoading).toBe(true);
    });
  });

  describe('CHANGE_PASSWORD_SUCCESS action', () => {
    it('should loading false', () => {
      const action = changePasswordSuccess();
      const state = authReducer(initialState, action);

      expect(state.isLoading).toBe(false);
    });
  });

  describe('CHANGE_PASSWORD_FAILURE action', () => {
    it('should loading false', () => {
      const action = changePasswordFail({ error: {} });
      const state = authReducer(initialState, action);

      expect(state.isLoading).toBe(false);
    });
  });
});

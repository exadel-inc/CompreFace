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

import {updateUserAuthorization, resetUserInfo, updateUserInfo} from './action';
import {UserInfoReducer} from './reducers';


describe('UserInfoReducer', () => {
  const initialState = {
    isAuthenticated: false,
    avatar: '',
    email: '',
    firstName: '',
    guid: '',
    userId: '',
    lastName: '',
    password: ''
  };

  describe('UpdateUserAuthorization action', () => {
    it('should set isAuthenticated to true', () => {
      const action = updateUserAuthorization({ value: true });
      const state = UserInfoReducer(initialState, action);

      expect(state.isAuthenticated).toBeTruthy();
    });
  });

  describe('ResetUserInfo action', () => {
    it('should should reset state to initial values', () => {
      const action = resetUserInfo();
      const state = UserInfoReducer({
        isAuthenticated: true,
        avatar: '',
        email: '',
        firstName: '',
        guid: '',
        userId: '',
        lastName: '',
        password: ''
      }, action);

      expect(state.firstName).toBeNull();
      expect(state.isAuthenticated).toBeFalsy();
    });
  });

  describe('UpdateUserInfo action', () => {
    it('should update user info according to payload', () => {
      const action = updateUserInfo({
        firstName: 'myTestUser2',
        isAuthenticated: true
      });
      const state = UserInfoReducer(initialState, action);

      expect(state.isAuthenticated).toBeTruthy();
      expect(state.firstName).toBe('myTestUser2');
    });
  });
});

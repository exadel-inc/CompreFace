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
import { routerReducer, RouterReducerState } from '@ngrx/router-store';

import { User } from '../data/interfaces/user';
import { authReducer, AuthState } from './auth/reducers';
import { RouterStateUrl } from './router/reducer';
import { serverStatus } from './servers-status/reducers';
import { userInfoReducer } from './userInfo/reducers';

export interface AppState {
  authState: AuthState;
  router: RouterReducerState<RouterStateUrl>;
  userInfo: User;
}

// feature reducer need to import into specific module on the page
// this for shared reducers:
export const sharedReducers = {
  auth: authReducer,
  serverStatus: serverStatus,
  router: routerReducer,
  userInfo: userInfoReducer,
};

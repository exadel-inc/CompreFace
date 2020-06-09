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

import {Store} from '@ngrx/store';
import {AppState} from 'src/app/store';
import {AuthService} from './auth.service';
import {updateUserInfo} from '../../store/userInfo/action';

export class AuthInit {
  constructor(private store: Store<AppState>, private auth: AuthService) {}

  public init(): void {
    const token: string = this.auth.getToken();
    const firstName: string = localStorage.getItem('firstName');

    if (token) {
      if (this.auth.isTokenValid(token)) {
        this.store.dispatch(updateUserInfo({
          isAuthenticated: true,
          firstName
        }));
      }
    }
  }
}

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

import {TestBed} from '@angular/core/testing';
import {AuthGuard, LoginGuard} from './auth.guard';
import {Router} from '@angular/router';
import {ROUTERS_URL} from '../../data/enums/routers-url.enum';
import {Store, MemoizedSelector} from '@ngrx/store';
import {provideMockStore, MockStore} from '@ngrx/store/testing';
import {AppState} from 'src/app/store';
import {selectUserInfoState} from '../../store/userInfo/selectors';
import {UserInfoState} from 'src/app/store/userInfo/reducers';

describe('Auth Guard', () => {
  let guard: AuthGuard;
  let router: Router;
  let mockStore: MockStore<AppState>;
  let mockUsernameSelector: MemoizedSelector<AppState, UserInfoState>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        provideMockStore(),
        {
          provide: Router,
          useValue: { navigateByUrl: () => { } }
        }
      ],
    });

    mockStore = TestBed.inject<MockStore<AppState>>(MockStore);
    mockUsernameSelector = mockStore.overrideSelector(selectUserInfoState, {
      avatar: '',
      email: '',
      firstName: '',
      guid: '',
      userId: '',
      lastName: '',
      password: '',
      isAuthenticated: true,
    });
    guard = TestBed.inject<AuthGuard>(AuthGuard);

    router = TestBed.inject<Router>(Router);
    router.navigateByUrl = jasmine.createSpy();
  });

  it('should return false if the user state is not logged in', () => {
    guard.canActivate().subscribe(value => {
      expect(value).toBeTruthy();
      expect(router.navigateByUrl).toHaveBeenCalledTimes(0);
    });
  });

  it('should return true if the user state is logged in', () => {
    mockUsernameSelector.setResult({
      avatar: '',
      email: '',
      firstName: '',
      guid: '',
      userId: '',
      lastName: '',
      password: '',
      isAuthenticated: false,
    });
    guard.canActivate().subscribe(value => {
      expect(value).toBeFalsy();
      expect(router.navigateByUrl).toHaveBeenCalledTimes(1);
      expect(router.navigateByUrl).toHaveBeenCalledWith(ROUTERS_URL.LOGIN);
    });
  });
});

describe('Login Guard', () => {
  let guard: LoginGuard;
  let router: Router;
  let mockStore: MockStore<AppState>;
  let mockUsernameSelector: MemoizedSelector<AppState, UserInfoState>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LoginGuard,
        provideMockStore(),
        {
          provide: Router,
          useValue: { navigateByUrl: () => { } }
        }
      ],
    });

    mockStore = TestBed.inject<MockStore<AppState>>(MockStore);
    mockUsernameSelector = mockStore.overrideSelector(selectUserInfoState, {
      avatar: '',
      email: '',
      firstName: '',
      guid: '',
      userId: '',
      lastName: '',
      password: '',
      isAuthenticated: false,
    });

    guard = TestBed.inject<LoginGuard>(LoginGuard);

    router = TestBed.inject<Router>(Router);
    router.navigateByUrl = jasmine.createSpy();
  });

  it('should return true if the user state is not logged in', () => {
    guard.canActivate().subscribe(value => {
      expect(value).toBeTruthy();
      expect(router.navigateByUrl).toHaveBeenCalledTimes(0);
    });
  });

  it('should return false if the user state is logged in', () => {
    mockUsernameSelector.setResult({
      avatar: '',
      email: '',
      firstName: '',
      guid: '',
      userId: '',
      lastName: '',
      password: '',
      isAuthenticated: true,
    });
    guard.canActivate().subscribe(value => {
      expect(value).toBeFalsy();
      expect(router.navigateByUrl).toHaveBeenCalledTimes(1);
      expect(router.navigateByUrl).toHaveBeenCalledWith(ROUTERS_URL.HOME);
    });
  });
});

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

import {Injectable} from '@angular/core';
import {Router, CanActivate} from '@angular/router';
import {Store} from '@ngrx/store';
import {ROUTERS_URL} from '../../data/enums/routers-url.enum';
import {AppState} from 'src/app/store';
import {selectUserInfoState} from '../../store/userInfo/selectors';
import {Observable} from 'rxjs';
import {take, map} from 'rxjs/operators';
import {UserInfoState} from 'src/app/store/userInfo/reducers';

@Injectable()
export class AuthGuard implements CanActivate {
  private userInfo: Observable<UserInfoState>;

  constructor(private router: Router, private store: Store<AppState>) {
    this.userInfo = this.store.select(selectUserInfoState);
  }

  canActivate(): Observable<boolean> {
    return this.userInfo.pipe(
      take(1),
      map((state: UserInfoState) => {
        if (!state.isAuthenticated) {
          this.router.navigateByUrl(ROUTERS_URL.LOGIN);
        }

        return !!state.isAuthenticated;
      })
    );
  }
}

@Injectable()
export class LoginGuard implements CanActivate {
  private userInfo$: Observable<UserInfoState>;

  constructor(private router: Router, private store: Store<AppState>) {
    this.userInfo$ = this.store.select(selectUserInfoState);
  }

  canActivate(): Observable<boolean> {
    return this.userInfo$.pipe(
      take(1),
      map((state: UserInfoState) => {
        if (state.isAuthenticated) {
          this.router.navigateByUrl(ROUTERS_URL.HOME);
        }

        return !state.isAuthenticated;
      })
    );
  }
}

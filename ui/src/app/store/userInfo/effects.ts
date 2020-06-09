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
import {Actions, Effect, ofType} from '@ngrx/effects';
import {of} from 'rxjs';
import {switchMap, catchError} from 'rxjs/operators';
import {
  getUserInfo,
  getUserInfoFail,
  getUserInfoSuccess
} from './action';
import {UserInfoService} from '../../core/user-info/user-info.service';

@Injectable()
export class UserInfoEffect {
  constructor(private actions: Actions, private userInfoService: UserInfoService) { }


  @Effect()
  getUser$ = this.actions.pipe(
    ofType(getUserInfo),
    switchMap(() => {
      return this.userInfoService.get().pipe(
        switchMap(user => [getUserInfoSuccess({ user })]),
        catchError(e => of(getUserInfoFail({ errorMessage: e })))
      );
    })
  );
}

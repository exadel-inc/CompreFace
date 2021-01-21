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
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';

import { UserInfoService } from '../../core/user-info/user-info.service';
import { SnackBarService } from '../../features/snackbar/snackbar.service';
import { refreshUserName } from '../user/action';
import { refreshApplication } from '../application/action';
import { getUserInfo, getUserInfoFail, getUserInfoSuccess, editUserInfo, editUserInfoSuccess, editUserInfoFail } from './action';

@Injectable()
export class UserInfoEffect {
  constructor(private actions: Actions, private userInfoService: UserInfoService, private snackBarService: SnackBarService) {}

  @Effect()
  getUser$ = this.actions.pipe(
    ofType(getUserInfo),
    switchMap(() =>
      this.userInfoService.get().pipe(
        map(user => getUserInfoSuccess({ user })),
        catchError(error => of(getUserInfoFail({ error })))
      )
    )
  );

  @Effect()
  editUserInfo$ = this.actions.pipe(
    ofType(editUserInfo),
    switchMap(({ firstName, lastName }) =>
      this.userInfoService.editUserInfo(firstName, lastName).pipe(
        switchMap(userInfo => [editUserInfoSuccess(userInfo), refreshUserName(userInfo), refreshApplication(userInfo)]),
        catchError(error => of(editUserInfoFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  editUserInfoSuccess$ = this.actions.pipe(
    ofType(editUserInfoSuccess),
    tap(() => this.snackBarService.openNotification({ messageText: 'org_users.edit_user_info' }))
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(getUserInfoFail, editUserInfoFail),
    tap(action => this.snackBarService.openHttpError(action.error))
  );
}

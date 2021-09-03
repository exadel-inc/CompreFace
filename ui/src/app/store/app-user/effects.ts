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
import { AppUserService } from 'src/app/core/app-user/app-user.service';
import { SnackBarService } from 'src/app/features/snackbar/snackbar.service';

import { loadApplications } from '../application/action';
import {
  addAppUserEntityAction,
  deleteUserFromApplication,
  deleteUserFromApplicationFail,
  deleteUserFromApplicationSuccess,
  inviteAppUser,
  inviteAppUserFail,
  inviteAppUserSuccess,
  loadAppUserEntityAction,
  updateAppUserRole,
  updateAppUserRoleFail,
  updateAppUserRoleSuccess,
} from './action';

@Injectable()
export class AppUserEffects {
  constructor(private actions: Actions, private appUserService: AppUserService, private snackBarService: SnackBarService) {}

  @Effect()
  loadAppUsers$ = this.actions.pipe(
    ofType(loadAppUserEntityAction),
    switchMap(action => this.appUserService.getAll(action.applicationId)),
    map(users => addAppUserEntityAction({ users }))
  );

  @Effect()
  updateUserRole$ = this.actions.pipe(
    ofType(updateAppUserRole),
    switchMap(({ applicationId, user }) =>
      this.appUserService.update(applicationId, user.id, user.role).pipe(
        switchMap(res => [updateAppUserRoleSuccess({ user: res }), loadApplications(), loadAppUserEntityAction({ applicationId })]),
        catchError(error => of(updateAppUserRoleFail({ error })))
      )
    )
  );

  @Effect()
  deleteAppUser$ = this.actions.pipe(
    ofType(deleteUserFromApplication),
    switchMap(action =>
      this.appUserService.deleteUser(action.applicationId, action.userId).pipe(
        map(() => deleteUserFromApplicationSuccess({ id: action.userId })),
        catchError(error => of(deleteUserFromApplicationFail({ error })))
      )
    )
  );

  @Effect()
  inviteAppUser$ = this.actions.pipe(
    ofType(inviteAppUser),
    switchMap(action =>
      this.appUserService.inviteUser(action.applicationId, action.userEmail, action.role).pipe(
        switchMap(() => [
          loadAppUserEntityAction({ applicationId: action.applicationId }),
          inviteAppUserSuccess({ userEmail: action.userEmail }),
        ]),
        catchError(error => of(inviteAppUserFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(deleteUserFromApplicationFail, updateAppUserRoleFail, inviteAppUserFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );

  @Effect({ dispatch: false })
  addUser$ = this.actions.pipe(
    ofType(inviteAppUserSuccess),
    tap(action => {
      this.snackBarService.openNotification({
        messageText: 'application_user_list.invitation_sent',
        messageOptions: { userEmail: action.userEmail },
      });
    })
  );
}

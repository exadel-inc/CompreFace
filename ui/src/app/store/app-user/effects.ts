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
import { Router } from '@angular/router';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { AppUserService } from 'src/app/core/app-user/app-user.service';
import { SnackBarService } from 'src/app/features/snackbar/snackbar.service';

import { Routes } from '../../data/enums/routers-url.enum';
import { loadApplications } from '../application/action';
import {
  addAppUserEntityAction,
  deleteUserFromApplication,
  deleteUserFromApplicationFail,
  deleteUserFromApplicationSuccess,
  inviteAppUserAction,
  inviteAppUserActionFail,
  inviteAppUserActionSuccess,
  loadAppUserEntityAction,
  updateAppUserRoleAction,
  updateAppUserRoleFailAction,
  updateAppUserRoleSuccessAction,
} from './actions';

@Injectable()
export class AppUserEffects {
  constructor(
    private actions: Actions,
    private appUserService: AppUserService,
    private snackBarService: SnackBarService,
    private router: Router
  ) {}

  @Effect()
  loadAppUsers = this.actions.pipe(
    ofType(loadAppUserEntityAction),
    switchMap(action => this.appUserService.getAll(action.applicationId)),
    map(users => addAppUserEntityAction({ users }))
  );

  @Effect()
  updateUserRole$ = this.actions.pipe(
    ofType(updateAppUserRoleAction),
    switchMap(({ applicationId, user }) =>
      this.appUserService.update(applicationId, user.id, user.role).pipe(
        switchMap(res => [updateAppUserRoleSuccessAction({ user: res }), loadApplications(), loadAppUserEntityAction({ applicationId })]),
        catchError(error => of(updateAppUserRoleFailAction({ error })))
      )
    )
  );

  @Effect()
  deleteAppUser$ = this.actions.pipe(
    ofType(deleteUserFromApplication),
    switchMap(action =>
      this.appUserService.deleteUser(action.applicationId, action.userId).pipe(
        map(() => deleteUserFromApplicationSuccess({ id: action.userId })),
        tap(() => this.router.navigateByUrl(Routes.Home)),
        catchError(error => of(deleteUserFromApplicationFail({ error })))
      )
    )
  );

  @Effect()
  inviteAppUser$ = this.actions.pipe(
    ofType(inviteAppUserAction),
    switchMap(action =>
      this.appUserService.inviteUser(action.applicationId, action.userEmail, action.role).pipe(
        switchMap(() => [
          loadAppUserEntityAction({ applicationId: action.applicationId }),
          inviteAppUserActionSuccess({ userEmail: action.userEmail }),
        ]),
        catchError(error => of(inviteAppUserActionFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(deleteUserFromApplicationFail, updateAppUserRoleFailAction, inviteAppUserActionFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );

  @Effect({ dispatch: false })
  addUser$ = this.actions.pipe(
    ofType(inviteAppUserActionSuccess),
    tap(action => {
      this.snackBarService.openNotification({
        messageText: 'application_user_list.invitation_sent',
        messageOptions: { userEmail: action.userEmail },
      });
    })
  );
}

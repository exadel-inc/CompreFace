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
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { AppUserService } from 'src/app/core/app-user/app-user.service';
import { SnackBarService } from 'src/app/features/snackbar/snackbar.service';
import { loadApplications } from 'src/app/store/application/action';

import {
  addAppUserEntityAction,
  deleteUserFromApplication,
  deleteUserFromApplicationFail,
  deleteUserFromApplicationSuccess,
  loadAppUserEntityAction,
  putUpdatedAppUserRoleEntityAction,
} from './actions';

@Injectable()
export class AppUserEffects {
  constructor(
    private actions: Actions,
    private appUserService: AppUserService,
    private snackBarService: SnackBarService,
  ) { }

  @Effect()
  loadAppUsers = this.actions.pipe(
    ofType(loadAppUserEntityAction),
    switchMap(action => this.appUserService.getAll(action.organizationId, action.applicationId)),
    map(users => addAppUserEntityAction({ users }))
  );

  @Effect()
  updateAppUser = this.actions.pipe(
    ofType(putUpdatedAppUserRoleEntityAction),
    switchMap(action => forkJoin([this.appUserService.update(
      action.organizationId,
      action.applicationId,
      action.user.id,
      action.user.role
    ), of(action)])),
    switchMap(observableResult => {
      const [user, action] = observableResult;
      return [
        loadAppUserEntityAction({ ...action }),
        loadApplications({ organizationId: action.organizationId })
      ];
    })
  );

  @Effect()
  deleteAppUser$ = this.actions.pipe(
    ofType(deleteUserFromApplication),
    switchMap((action =>
      this.appUserService.deleteUser(action.organizationId, action.applicationId, action.userId).pipe(
        map(() => deleteUserFromApplicationSuccess({ id: action.userId })),
        catchError(error => of(deleteUserFromApplicationFail({ error }))),
      )),
    )
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(deleteUserFromApplicationFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );
}

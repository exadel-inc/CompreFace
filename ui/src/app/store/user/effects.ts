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
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { UserService } from 'src/app/core/user/user.service';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { loadApplications } from 'src/app/store/application/action';
import { fetchRolesEntityAction, loadRolesEntityAction } from 'src/app/store/role/actions';
import {
  addUsersEntityAction,
  deleteUser,
  deleteUserFail,
  deleteUserSuccess,
  loadUsersEntityAction,
  updateUserRoleAction,
  updateUserRoleFailAction,
  updateUserRoleSuccessAction,
  updateUserRoleWithRefreshAction,
} from 'src/app/store/user/action';

import { AppState } from '..';
import { AuthService } from '../../core/auth/auth.service';
import { SnackBarService } from '../../features/snackbar/snackbar.service';

@Injectable()
export class UserListEffect {
  constructor(
    private actions: Actions,
    private userService: UserService,
    private authService: AuthService,
    private snackBarService: SnackBarService,
    private store: Store<AppState>
  ) {}

  @Effect()
  fetchUserList = this.actions.pipe(
    ofType(loadUsersEntityAction),
    switchMap(action => this.userService.getAll()),
    map((users: AppUser[]) => addUsersEntityAction({ users }))
  );

  @Effect()
  updateUserRole = this.actions.pipe(
    ofType(updateUserRoleAction),
    switchMap(({ user }) =>
      this.userService.updateRole(user.id, user.role).pipe(
        map(res => updateUserRoleSuccessAction({ user: res })),
        catchError(error => of(updateUserRoleFailAction({ error })))
      )
    )
  );

  @Effect()
  updateUserRoleWithRefresh = this.actions.pipe(
    ofType(updateUserRoleWithRefreshAction),
    switchMap(({ user }) =>
      this.userService.updateRole(user.id, user.role).pipe(
        switchMap(res => [updateUserRoleSuccessAction({ user: res }), loadUsersEntityAction()]),
        catchError(error => of(updateUserRoleFailAction({ error })))
      )
    )
  );

  @Effect()
  deleteUser$ = this.actions.pipe(
    ofType(deleteUser),
    switchMap(({ userId, deleterUserId }) =>
      this.userService.delete(userId).pipe(
        switchMap(() => {
          if (deleterUserId === userId) {
            this.authService.logOut();
            return [];
          }
          return [deleteUserSuccess({ userId }), loadApplications(), loadUsersEntityAction()];
        }),
        catchError(error => of(deleteUserFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(deleteUserFail, updateUserRoleFailAction),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );

  @Effect()
  fetchAvailableRoles = this.actions.pipe(
    ofType(loadRolesEntityAction),
    switchMap(() => this.userService.fetchAvailableRoles()),
    // workaround until backend doesnt support available roles call
    catchError(x => of(['OWNER', 'ADMIN', 'USER'])),
    map(rolesArray => fetchRolesEntityAction({ role: { id: 0, accessLevels: rolesArray } }))
  );
}

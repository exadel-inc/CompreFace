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
import { UserService } from 'src/app/core/user/user.service';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { loadApplications } from 'src/app/store/application/action';
import { fetchRolesEntity, loadRolesEntity } from 'src/app/store/role/action';
import {
  addUsersEntity,
  deleteUser,
  deleteUserFail,
  deleteUserSuccess,
  loadUsersEntity,
  updateUserRole,
  updateUserRoleFail,
  updateUserRoleSuccess,
  updateUserRoleWithRefresh,
} from 'src/app/store/user/action';

import { AuthService } from '../../core/auth/auth.service';
import { SnackBarService } from '../../features/snackbar/snackbar.service';
import { logOut } from '../auth/action';

@Injectable()
export class UserListEffect {
  constructor(
    private actions: Actions,
    private userService: UserService,
    private authService: AuthService,
    private snackBarService: SnackBarService
  ) {}

  @Effect()
  fetchUserList$ = this.actions.pipe(
    ofType(loadUsersEntity),
    switchMap(action => this.userService.getAll()),
    map((users: AppUser[]) => addUsersEntity({ users }))
  );

  @Effect()
  updateUserRole$ = this.actions.pipe(
    ofType(updateUserRole),
    switchMap(({ user }) =>
      this.userService.updateRole(user.id, user.role).pipe(
        map(res => updateUserRoleSuccess({ user: res })),
        catchError(error => of(updateUserRoleFail({ error })))
      )
    )
  );

  @Effect()
  updateUserRoleWithRefresh$ = this.actions.pipe(
    ofType(updateUserRoleWithRefresh),
    switchMap(({ user }) =>
      this.userService.updateRole(user.id, user.role).pipe(
        switchMap(res => [updateUserRoleSuccess({ user: res }), loadUsersEntity()]),
        catchError(error => of(updateUserRoleFail({ error })))
      )
    )
  );

  @Effect()
  deleteUser$ = this.actions.pipe(
    ofType(deleteUser),
    switchMap(({ userId, deleterUserId, newOwner }) =>
      this.userService.delete(userId, newOwner).pipe(
        switchMap(() => {
          if (deleterUserId === userId) {
            this.authService.logOut();
            return [logOut()];
          }
          return [deleteUserSuccess({ userId }), loadApplications(), loadUsersEntity()];
        }),
        catchError(error => of(deleteUserFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(deleteUserFail, updateUserRoleFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );

  @Effect()
  fetchAvailableRoles$ = this.actions.pipe(
    ofType(loadRolesEntity),
    switchMap(() => this.userService.fetchAvailableRoles()),
    // workaround until backend doesnt support available roles call
    catchError(x => of(['OWNER', 'ADMIN', 'USER'])),
    map(rolesArray => fetchRolesEntity({ role: { id: 0, accessLevels: rolesArray } }))
  );
}

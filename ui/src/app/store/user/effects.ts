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
import {Injectable} from '@angular/core';
import { merge } from 'rxjs';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {UserService} from 'src/app/core/user/user.service';
import {
  addUsersEntityAction,
  deleteUser,
  deleteUserFail,
  deleteUserSuccess,
  loadUsersEntityAction,
  updateUserRoleAction,
  updateUserRoleFailAction,
  updateUserRoleSuccessAction, updateUserRoleWithRefreshAction
} from 'src/app/store/user/action';
import {loadApplications} from 'src/app/store/application/action';
import {catchError, map, switchMap, tap} from 'rxjs/operators';
import {AppUser} from 'src/app/data/appUser';
import {fetchRolesEntityAction, loadRolesEntityAction} from 'src/app/store/role/actions';
import {of} from 'rxjs';
import {SnackBarService} from '../../features/snackbar/snackbar.service';
import {OrganizationEnService} from '../organization/organization-entitys.service';
import {AppState} from '../index';
import {AuthService} from '../../core/auth/auth.service';

@Injectable()
export class UserListEffect {
    constructor(
        private actions: Actions,
        private userService: UserService,
        private authService: AuthService,
        private organizationEnService: OrganizationEnService,
        private snackBarService: SnackBarService,
        private store: Store<AppState>
    ) {
    }

    @Effect()
    fetchUserList =
        this.actions.pipe(
            ofType(loadUsersEntityAction),
            switchMap((action) => this.userService.getAll(action.organizationId)),
            map((users: AppUser[]) => addUsersEntityAction({ users }))
        );

  @Effect()
  updateUserRole = this.actions.pipe(
    ofType(updateUserRoleAction),
    switchMap(({ organizationId, user }) =>
      this.userService.updateRole(organizationId, user.id, user.role).pipe(
        map(res => (updateUserRoleSuccessAction({user: res}))),
        catchError((error) => of(updateUserRoleFailAction({ error })))
      )));

  @Effect()
  updateUserRoleWithRefresh = this.actions.pipe(
    ofType(updateUserRoleWithRefreshAction),
    switchMap(({ organizationId, user }) =>
      this.userService.updateRole(organizationId, user.id, user.role).pipe(
        switchMap((res) => merge(
          of(updateUserRoleSuccessAction({user: res})),
          of(loadUsersEntityAction({organizationId}))
        )),
        catchError((error) => of(updateUserRoleFailAction({ error })))
      )));

    @Effect()
    deleteUser$ = this.actions.pipe(
      ofType(deleteUser),
      switchMap(({ organizationId, userId, newOwner, deleterUserId}) =>
        this.userService.delete(organizationId, userId, newOwner).pipe(
          switchMap(() => {
            if (deleterUserId === userId) {
                this.authService.logOut();
                return [];
            }
            return [
              deleteUserSuccess({ userId }),
              loadApplications({ organizationId }),
              loadUsersEntityAction({ organizationId }),
              catchError(error => of(deleteUserFail({ error })))
          ]; }),
        )
      ),
    );

    @Effect({ dispatch: false })
    showError$ = this.actions.pipe(
      ofType(deleteUserFail, updateUserRoleFailAction),
      tap(action => {
        this.snackBarService.openHttpError(action.error);
      })
    );

    @Effect()
    fetchAvailableRoles = this.actions
        .pipe(
            ofType(loadRolesEntityAction),
            switchMap(() => this.userService.fetchAvailableRoles()),
            // workaround until backend doesnt support available roles call
            catchError(x => of(['OWNER', 'ADMIN', 'USER'])),
            map((rolesArray) => fetchRolesEntityAction({ role: { id: 0, accessLevels: rolesArray } }))
        );

}

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

import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { UserService } from 'src/app/core/user/user.service';
import {
  addUsersEntityAction,
  loadUsersEntityAction,
  deleteUser,
  updateUserRoleSuccessAction,
  updateUserRoleFailAction,
  updateUserRoleAction
} from 'src/app/store/user/action';
import { switchMap, map, catchError, filter, tap } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';
import { fetchRolesEntityAction, loadRolesEntityAction } from 'src/app/store/role/actions';
import { forkJoin, of } from 'rxjs';
import { SnackBarService } from '../../features/snackbar/snackbar.service';
import { OrganizationEnService } from '../organization/organization-entitys.service';

@Injectable()
export class UserListEffect {
    constructor(
        private actions: Actions,
        private userService: UserService,
        private organizationEnService: OrganizationEnService,
        private snackBarService: SnackBarService,
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
    deleteUser$ =
        this.actions.pipe(
            ofType(deleteUser),
            switchMap(action => forkJoin([
                this.userService.delete(action.organizationId, action.userId, action.newOwner),
                of(action.organizationId)
            ]).pipe(
                catchError((error: HttpErrorResponse) => of(this.snackBarService.openHttpError(error))),
            )),
            filter((data: any) => !!data),
            map(([deleteResult, organizationId]) => loadUsersEntityAction({ organizationId })),
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

    @Effect({ dispatch: false })
    showError$ = this.actions.pipe(
      ofType(updateUserRoleFailAction),
      tap(action => {
        this.snackBarService.openHttpError(action.error);
      })
    );
}

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
import { Store } from '@ngrx/store';
import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { UserService } from 'src/app/core/user/user.service';
import {
    AddUsersEntityAction,
    LoadUsersEntityAction,
    PutUpdatedUserRoleEntityAction,
    DeleteUser,
    DeleteUserFail
} from 'src/app/store/user/action';
import { loadApplications } from 'src/app/store/application/action';
import { switchMap, map, catchError, filter, tap } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';
import { FetchRolesEntityAction, LoadRolesEntityAction } from 'src/app/store/role/actions';
import { forkJoin, of } from 'rxjs';
import { SnackBarService } from '../../features/snackbar/snackbar.service';
import { OrganizationEnService } from '../organization/organization-entitys.service';
import {AppState} from '../index';

@Injectable()
export class UserListEffect {
    constructor(
        private actions: Actions,
        private userService: UserService,
        private organizationEnService: OrganizationEnService,
        private snackBarService: SnackBarService,
        private store: Store<AppState>
    ) {
    }

    @Effect()
    fetchUserList =
        this.actions.pipe(
            ofType(LoadUsersEntityAction),
            switchMap((action) => this.userService.getAll(action.organizationId)),
            map((users: AppUser[]) => AddUsersEntityAction({ users }))
        );

    @Effect()
    UpdateUserRole =
        this.actions.pipe(
            ofType(PutUpdatedUserRoleEntityAction),
            switchMap((action) => forkJoin([this.userService.updateRole(
                action.organizationId,
                action.user.id,
                action.user.role
            ), of(action.organizationId)])),
            switchMap(res => {
                const [user, organizationId] = res;
                this.organizationEnService.getAll();

                return [LoadUsersEntityAction({ organizationId })];
            })
        );

    @Effect()
    deleteUser$ =
        this.actions.pipe(
            ofType(DeleteUser),
            switchMap(action => forkJoin([
                this.userService.delete(action.organizationId, action.userId, action.newOwner).pipe(
                  map(() => {
                    this.store.dispatch(
                      loadApplications({ organizationId: action.organizationId })
                    );
                }),
                  catchError(error => of(DeleteUserFail({error})))
                ),
                of(action.organizationId)
            ]).pipe(
                catchError((error: HttpErrorResponse) => of(this.snackBarService.openHttpError(error))),
            )),
            filter((data: any) => !!data),
            map(([deleteResult, organizationId]) => LoadUsersEntityAction({ organizationId })),
        );

    @Effect()
    FetchAvailableRoles = this.actions
        .pipe(
            ofType(LoadRolesEntityAction),
            switchMap(() => this.userService.fetchAvailableRoles()),
            // workaround until backend doesnt support available roles call
            catchError(x => of(['OWNER', 'ADMIN', 'USER'])),
            map((rolesArray) => FetchRolesEntityAction({ role: { id: 0, accessLevels: rolesArray } }))
        );
}

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
import { Store } from '@ngrx/store';
import { combineLatest, Observable, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { IFacade } from 'src/app/data/interfaces/IFacade';
import { UserDeletion } from 'src/app/data/interfaces/user-deletion';
import { AppState } from 'src/app/store';
import { loadRolesEntityAction } from 'src/app/store/role/actions';
import { selectAllRoles, selectIsPendingRoleStore } from 'src/app/store/role/selectors';
import { deleteUser, loadUsersEntityAction, updateUserRoleWithRefreshAction } from 'src/app/store/user/action';
import { selectCurrentUserRole, selectIsPendingUserStore, selectUsers, selectUsersWithOwnerApp } from 'src/app/store/user/selectors';
import { selectUserEmail, selectUserId } from 'src/app/store/userInfo/selectors';
import { Role } from '../../data/enums/role.enum';

@Injectable()
export class UserListFacade implements IFacade {
  users$: Observable<AppUser[]>;
  availableRoles$: Observable<string[]>;
  isLoading$: Observable<boolean>;
  currentUserId$: Observable<string>;
  currentUserEmail$: Observable<string>;
  userRole$: Observable<string>;

  constructor(private store: Store<AppState>) {
    this.users$ = this.store.select(selectUsersWithOwnerApp);
    this.userRole$ = this.store.select(selectCurrentUserRole);

    const allRoles$ = this.store.select(selectAllRoles);
    this.availableRoles$ = combineLatest([allRoles$, this.userRole$]).pipe(
      map(([allRoles, userRole]) => {
        const roleIndex = allRoles.indexOf(userRole);
        return roleIndex !== -1 ? allRoles.slice(0, roleIndex + 1) : [];
      })
    );

    const usersLoading$ = this.store.select(selectIsPendingUserStore);
    const roleLoading$ = this.store.select(selectIsPendingRoleStore);
    this.currentUserId$ = this.store.select(selectUserId);
    this.currentUserEmail$ = this.store.select(selectUserEmail);

    this.isLoading$ = combineLatest([usersLoading$, roleLoading$]).pipe(map(observResults => !(!observResults[0] && !observResults[1])));
  }

  initSubscriptions(): void {
    this.loadUsers();
    this.loadAvailableRoles();
  }

  loadUsers(): void {
    this.store.dispatch(loadUsersEntityAction());
  }

  updateUserRole(id: string, role: Role): void {
    this.store.dispatch(
      updateUserRoleWithRefreshAction({
        user: {
          id,
          role,
        },
      })
    );
  }

  deleteUser(deletion: UserDeletion): void {
    this.store.dispatch(
      deleteUser({
        userId: deletion.userToDelete.userId,
        deleterUserId: deletion.deleterUserId,
      })
    );
  }

  loadAvailableRoles(): void {
    this.store.dispatch(loadRolesEntityAction());
  }
}

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
import { Observable } from 'rxjs';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { IFacade } from 'src/app/data/interfaces/IFacade';
import { UserDeletion } from 'src/app/data/interfaces/user-deletion';
import { AppState } from 'src/app/store';
import { loadRolesEntity } from 'src/app/store/role/action';
import { deleteUser, loadUsersEntity, updateUserRoleWithRefresh } from 'src/app/store/user/action';
import { selectCurrentUserRole, selectUsersWithOwnerApp, selectAvailableRoles, selectIsUserLoading } from 'src/app/store/user/selectors';
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
    this.isLoading$ = this.store.select(selectIsUserLoading);
    this.availableRoles$ = this.store.select(selectAvailableRoles);

    this.currentUserId$ = this.store.select(selectUserId);
    this.currentUserEmail$ = this.store.select(selectUserEmail);
  }

  initSubscriptions(): void {
    this.loadUsers();
    this.loadAvailableRoles();
  }

  loadUsers(): void {
    this.store.dispatch(loadUsersEntity());
  }

  updateUserRole(id: string, role: Role): void {
    this.store.dispatch(
      updateUserRoleWithRefresh({
        user: {
          id,
          role,
        },
      })
    );
  }

  deleteUser(deletion: UserDeletion, newOwner: string): void {
    this.store.dispatch(
      deleteUser({
        userId: deletion.userToDelete.userId,
        deleterUserId: deletion.deleterUserId,
        newOwner,
      })
    );
  }

  loadAvailableRoles(): void {
    this.store.dispatch(loadRolesEntity());
  }
}

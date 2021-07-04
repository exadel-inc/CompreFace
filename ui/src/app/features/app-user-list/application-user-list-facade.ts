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
import { Observable, Subscription } from 'rxjs';
import { AppUser } from 'src/app/data/interfaces/app-user';
import { IFacade } from 'src/app/data/interfaces/IFacade';
import { AppState } from 'src/app/store';
import { deleteUserFromApplication, inviteAppUser, loadAppUserEntityAction, updateAppUserRole } from 'src/app/store/app-user/action';
import {
  selectAppUsers,
  selectAvailableEmails,
  selectAvailableRoles,
  selectIsApplicationLoading,
  selectUserRole,
} from 'src/app/store/app-user/selectors';
import { selectCurrentApp, selectUserRollForSelectedApp } from 'src/app/store/application/selectors';
import { loadRolesEntity } from 'src/app/store/role/action';
import { selectUserId } from 'src/app/store/userInfo/selectors';

import { loadUsersEntity } from '../../store/user/action';
import { selectCurrentUserRole } from '../../store/user/selectors';
import { Role } from 'src/app/data/enums/role.enum';

@Injectable()
export class ApplicationUserListFacade implements IFacade {
  isLoading$: Observable<boolean>;
  appUsers$: Observable<AppUser[]>;
  availableRoles$: Observable<string[]>;
  availableEmails$: Observable<string[]>;
  userRole$: Observable<string>;
  currentUserId$: Observable<string>;
  selectedApplicationName: string;

  private selectedApplicationId: string;
  private sub: Subscription;
  userGlobalRole$: Observable<Role>;
  applicationRole$: Observable<string>;

  constructor(private store: Store<AppState>) {
    this.appUsers$ = this.store.select(selectAppUsers);
    this.availableEmails$ = this.store.select(selectAvailableEmails);
    this.userGlobalRole$ = this.store.select(selectCurrentUserRole);
    this.applicationRole$ = this.store.select(selectUserRollForSelectedApp);
    this.userRole$ = this.store.select(selectUserRole);
    this.currentUserId$ = this.store.select(selectUserId);
    this.availableRoles$ = this.store.select(selectAvailableRoles);
    this.isLoading$ = this.store.select(selectIsApplicationLoading);
  }

  initSubscriptions(): void {
    this.sub = this.store.select(selectCurrentApp).subscribe(app => {
      if (app) {
        this.selectedApplicationId = app.id;
        this.selectedApplicationName = app.name;
        this.loadData();
      }
    });
  }

  loadData(): void {
    this.store.dispatch(
      loadAppUserEntityAction({
        applicationId: this.selectedApplicationId,
      })
    );
    this.store.dispatch(loadRolesEntity());
    this.store.dispatch(loadUsersEntity());
  }

  updateUserRole(id: string, role: Role): void {
    this.store.dispatch(
      updateAppUserRole({
        applicationId: this.selectedApplicationId,
        user: {
          id,
          role,
        },
      })
    );
  }

  inviteUser(email: string, role: Role): void {
    this.store.dispatch(
      inviteAppUser({
        applicationId: this.selectedApplicationId,
        userEmail: email,
        role,
      })
    );
  }

  delete(userId: string) {
    this.store.dispatch(
      deleteUserFromApplication({
        applicationId: this.selectedApplicationId,
        userId,
      })
    );
  }

  unsubscribe(): void {
    this.sub.unsubscribe();
  }
}

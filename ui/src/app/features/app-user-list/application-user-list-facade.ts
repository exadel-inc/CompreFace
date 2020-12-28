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
import { AppState } from 'src/app/store';
import {
  deleteUserFromApplication,
  inviteAppUserAction,
  loadAppUserEntityAction,
  updateAppUserRoleAction,
} from 'src/app/store/app-user/actions';
import { selectAppUserIsPending, selectAppUsers } from 'src/app/store/app-user/selectors';
import { selectCurrentApp, selectUserRollForSelectedApp } from 'src/app/store/application/selectors';
import { loadRolesEntityAction } from 'src/app/store/role/actions';
import { selectAllRoles, selectIsPendingRoleStore } from 'src/app/store/role/selectors';
import { selectUserId } from 'src/app/store/userInfo/selectors';

import { AppUserService } from '../../core/app-user/app-user.service';
import { loadUsersEntityAction } from '../../store/user/action';
import { selectCurrentUserRole, selectUsers } from '../../store/user/selectors';
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

  constructor(private store: Store<AppState>, private userService: AppUserService) {
    this.appUsers$ = this.store.select(selectAppUsers);
    this.availableEmails$ = combineLatest([this.store.select(selectUsers), this.appUsers$]).pipe(
      map(([users, appUsers]) =>
        users.map(user => {
          if (appUsers.every(appUser => appUser.id !== user.id)) {
            return user.email;
          }
        })
      )
    );

    this.userGlobalRole$ = this.store.select(selectCurrentUserRole);
    this.applicationRole$ = this.store.select(selectUserRollForSelectedApp);
    this.userRole$ = combineLatest([this.store.select(selectUserRollForSelectedApp), this.userGlobalRole$]).pipe(
      map(([applicationRole, globalRole]) => {
        // the global role (if OWNER or ADMINISTRATOR) should prevail on the application role
        if (globalRole !== Role.User) {
          if (globalRole === Role.Owner) {
            return globalRole;
          }

          if (globalRole === Role.Administrator) {
            return applicationRole === Role.Owner ? applicationRole : globalRole;
          }
        }
        return applicationRole === Role.Owner ? applicationRole : globalRole;
      })
    );

    this.currentUserId$ = this.store.select(selectUserId);
    const allRoles$ = this.store.select(selectAllRoles);

    this.availableRoles$ = combineLatest([allRoles$, this.userRole$, this.applicationRole$, this.userGlobalRole$]).pipe(
      map(([allRoles, userRole, applicationRole, globalRole]) => {
        if (globalRole === Role.Owner || applicationRole === Role.Owner) {
          return allRoles;
        } else if (globalRole === Role.Administrator) {
          return allRoles;
        } else {
          const roleIndex = allRoles.indexOf(userRole);
          return roleIndex !== -1 ? allRoles.slice(0, roleIndex + 1) : [];
        }
      })
    );

    const usersLoading$ = this.store.select(selectAppUserIsPending);
    const roleLoading$ = this.store.select(selectIsPendingRoleStore);

    this.isLoading$ = combineLatest([usersLoading$, roleLoading$]).pipe(map(observResults => !(!observResults[0] && !observResults[1])));
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
    this.store.dispatch(loadRolesEntityAction());
    this.store.dispatch(loadUsersEntityAction());
  }

  updateUserRole(id: string, role: Role): void {
    this.store.dispatch(
      updateAppUserRoleAction({
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
      inviteAppUserAction({
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

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
import { combineLatest, Observable, Subscription, zip } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';
import { IFacade } from 'src/app/data/facade/IFacade';
import { AppState } from 'src/app/store';
import {
  deleteUserFromApplication,
  loadAppUserEntityAction,
  putUpdatedAppUserRoleEntityAction,
} from 'src/app/store/app-user/actions';
import { selectAppUserIsPending, selectAppUsers } from 'src/app/store/app-user/selectors';
import { selectCurrentApp, selectUserRollForSelectedApp } from 'src/app/store/application/selectors';
import { selectCurrentOrganizationId, selectUserRollForSelectedOrganization } from 'src/app/store/organization/selectors';
import { LoadRolesEntityAction } from 'src/app/store/role/actions';
import { selectAllRoles, selectIsPendingRoleStore } from 'src/app/store/role/selectors';
import { selectUserId } from 'src/app/store/userInfo/selectors';

import { AppUserService } from '../../core/app-user/app-user.service';
import { LoadUsersEntityAction } from '../../store/user/action';
import { selectUsers } from '../../store/user/selectors';

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
  private selectedOrganizationId: string;
  private sub: Subscription;

  constructor(private store: Store<AppState>, private userService: AppUserService) {
    this.appUsers$ = store.select(selectAppUsers);
    this.availableEmails$ = store.select(selectUsers).pipe(map(data => data.map(user => user.email)));
    this.userRole$ = combineLatest(
      this.store.select(selectUserRollForSelectedApp),
      this.store.select(selectUserRollForSelectedOrganization)).pipe(
      map(([applicationRole, organizationRole]) => {
       // the organization role (if OWNER or ADMINISTRATOR) should prevail on the application role
       return organizationRole !== "USER" ? organizationRole : applicationRole
      })
    );

    this.currentUserId$ = store.select(selectUserId);
    const allRoles$ = store.select(selectAllRoles);

    this.availableRoles$ = combineLatest(allRoles$, this.userRole$).pipe(
      map(([allRoles, userRole]) => {
        const roleIndex = allRoles.indexOf(userRole);
        return roleIndex !== -1 ? allRoles.slice(0, roleIndex + 1) : [];
      }),
    );

    const usersLoading$ = store.select(selectAppUserIsPending);
    const roleLoading$ = store.select(selectIsPendingRoleStore);

    this.isLoading$ = combineLatest(usersLoading$, roleLoading$)
      .pipe(
        map(observResults => !(!observResults[0] && !observResults[1])
        ));
  }

  initSubscriptions(): void {
    this.sub = zip(
      this.store.select(selectCurrentApp),
      this.store.select(selectCurrentOrganizationId)
    ).subscribe(([app, orgId]) => {
      if (app && orgId) {
        this.selectedApplicationId = app.id;
        this.selectedApplicationName = app.name;
        this.selectedOrganizationId = orgId;
        this.loadData();
      }
    });
  }

  loadData(): void {
    this.store.dispatch(loadAppUserEntityAction({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId
    }));
    this.store.dispatch(LoadRolesEntityAction());
    this.store.dispatch(LoadUsersEntityAction({
      organizationId: this.selectedOrganizationId
    }));
  }

  updateUserRole(id: string, role: string): void {
    this.store.dispatch(putUpdatedAppUserRoleEntityAction({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      user: {
        id,
        role
      }
    }));
  }

  inviteUser(email: string, role: string): Observable<any> {
    return this.userService.inviteUser(this.selectedOrganizationId, this.selectedApplicationId, email, role)
      .pipe(tap(() =>
        this.store.dispatch(loadAppUserEntityAction({
          organizationId: this.selectedOrganizationId,
          applicationId: this.selectedApplicationId
        }))
      ));
  }

  delete(userId: string) {
    this.store.dispatch(deleteUserFromApplication({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      userId,
    }));
  }

  unsubscribe(): void {
    this.sub.unsubscribe();
  }
}

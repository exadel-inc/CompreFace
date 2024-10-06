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
import { createFeatureSelector, createSelector } from '@ngrx/store';

import { appUserAdapter, AppUserEntityState } from './reducers';
import { selectCurrentUserRole, selectUsers } from '../user/selectors';
import { Role } from '../../data/enums/role.enum';
import { selectUserRollForSelectedApp } from '../application/selectors';
import { selectAllRoles, selectIsPendingRoleStore } from '../role/selectors';

const { selectAll } = appUserAdapter.getSelectors();

export const selectAppUserEntityState = createFeatureSelector<AppUserEntityState>('app-user');
export const selectAppUserIsPending = createSelector(selectAppUserEntityState, state => state.isPending);
export const selectAppUsers = createSelector(selectAppUserEntityState, selectAll);
export const selectAvailableEmails = createSelector(selectUsers, selectAppUsers, (users, appUsers) => {
  const emails = [];
  users.map(user => {
    if (appUsers.every(appUser => appUser.id !== user.id)) {
      emails.push(user.email);
    }
  });
  return emails;
});

export const selectUserRole = createSelector(selectUserRollForSelectedApp, selectCurrentUserRole, (applicationRole, globalRole) => {
  if (globalRole !== Role.User) {
    if (globalRole === Role.Owner) {
      return globalRole;
    }

    if (globalRole === Role.Administrator) {
      return applicationRole === Role.Owner ? applicationRole : globalRole;
    }
  }
  return applicationRole === Role.Owner ? applicationRole : globalRole;
});
export const selectAvailableRoles = createSelector(
  selectAllRoles,
  selectUserRole,
  selectUserRollForSelectedApp,
  selectCurrentUserRole,
  (allRoles, userRole, applicationRole, globalRole) => {
    if (globalRole === Role.Owner || applicationRole === Role.Owner) {
      return allRoles;
    } else if (globalRole === Role.Administrator) {
      return allRoles;
    } else {
      const roleIndex = allRoles.indexOf(userRole);
      return roleIndex !== -1 ? allRoles.slice(0, roleIndex + 1) : [];
    }
  }
);
export const selectIsApplicationLoading = createSelector(
  selectAppUserIsPending,
  selectIsPendingRoleStore,
  (appUserIsPending, isPendingRoleStore) => !(!appUserIsPending && !isPendingRoleStore)
);

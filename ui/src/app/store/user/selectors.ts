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

import { selectApplications, selectIsPendingApplicationList } from '../application/selectors';
import { selectUserId } from '../userInfo/selectors';
import { AppUserEntityState, userAdapter } from './reducers';

export const selectUserEntityState = createFeatureSelector<AppUserEntityState>('user');
const { selectEntities, selectAll } = userAdapter.getSelectors();

export const selectUserById = (id: string) => createSelector(selectUserEntityState, selectEntities, usersDictionary => usersDictionary[id]);
export const selectUsers = createSelector(selectUserEntityState, selectAll);
export const selectIsPendingUserStore = createSelector(selectUserEntityState, state => state.isPending);

export const selectCurrentUserRole = createSelector(selectUsers, selectUserId, (users, userId) => {
  const selectedUser = users.find(user => user.userId === userId);
  return selectedUser && selectedUser.role;
});

export const selectUsersWithOwnerApp = createSelector(selectUsers, selectApplications, (users, apps) =>
  users.map(user => ({
    ...user,
    ownerOfApplications: apps.filter(app => app.owner.userId === user.userId).map(app => app.name),
  }))
);

export const selectIsLoadingApplicationList = createSelector(selectIsPendingApplicationList, selectCurrentUserRole,
  (isPendingAppList, currentUserRole) => (!isPendingAppList && !currentUserRole));

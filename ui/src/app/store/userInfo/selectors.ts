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

import { User } from '../../data/interfaces/user';

export const selectUserInfoState = createFeatureSelector<User>('userInfo');

export const selectUserId = createSelector(selectUserInfoState, userInfo => userInfo?.userId);

export const selectUserEmail = createSelector(selectUserInfoState, userInfo => userInfo.email);

// TODO: move default avatar to backend response
export const selectUserAvatar = createSelector(selectUserInfoState, userInfo => userInfo.avatar || 'assets/img/avatar.svg');

export const selectUserFirstName = createSelector(selectUserInfoState, userInfo => userInfo.firstName);

export const selectUserLastName = createSelector(selectUserInfoState, userInfo => userInfo.lastName);

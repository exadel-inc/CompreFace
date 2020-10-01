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

import {createSelector, createFeatureSelector} from '@ngrx/store';
import * as FromOrganization from './reducers';
import {Organization} from '../../data/interfaces/organization';
import {EntitySelectorsFactory} from '@ngrx/data';

export const organizationSelectors = new EntitySelectorsFactory().create<any>('Organization');

export const selectOrganizationState = createFeatureSelector<FromOrganization.OrganizationsState>('Organization');

export const selectCurrentOrganizationId = createSelector(
  selectOrganizationState,
  state => state.selectId
);

export const selectSelectedOrganization = createSelector(
  organizationSelectors.selectEntities,
  selectCurrentOrganizationId,
  (organizationCash, selectId) => organizationCash.find(org => org.id === selectId)
);

export const selectUserRollForSelectedOrganization = createSelector(
  selectSelectedOrganization,
  organization => organization ? organization.role : null
);

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

import {
  selectCurrentOrganizationId,
  selectSelectedOrganization,
  selectUserRollForSelectedOrganization
} from './selectors';
import {Organization} from '../../data/interfaces/organization';

describe('OrganizationSelectors', () => {

  it('selectCurrentOrganizationId', () => {
    expect(selectCurrentOrganizationId.projector({selectId: 'someId'})).toBe('someId');
  });

  it('SelectSelectedOrganization', () => {
    const org = [{id: 1, name: 'name1'}, {id: 2, name: 'name2'}];
    expect(selectSelectedOrganization.projector(org, 1)).toEqual({id: 1, name: 'name1'});
    expect(selectSelectedOrganization.projector(org, 2)).toEqual({id: 2, name: 'name2'});
    expect(selectSelectedOrganization.projector(org, 0)).toBeFalsy();
  });

  it('SelectUserRollForSelectedOrganization', () => {
    const org1: Organization = {
      id: '2',
      name: 'name1',
      role: 'ADMIN'
    };

    const org2: Organization = {
      id: '2',
      name: 'name1',
      role: 'OWNER'
    };
    expect(selectUserRollForSelectedOrganization.projector(org1)).toBe('ADMIN');
    expect(selectUserRollForSelectedOrganization.projector(org2)).toBe('OWNER');
    expect(selectUserRollForSelectedOrganization.projector(null)).toBeFalsy();
  });
});

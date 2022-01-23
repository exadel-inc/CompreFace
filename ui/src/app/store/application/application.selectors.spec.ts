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
import { Role } from 'src/app/data/enums/role.enum';

import { Application } from '../../data/interfaces/application';
import { selectCurrentApp, selectCurrentAppId, selectUserRollForSelectedApp } from './selectors';

describe('ApplicationSelectors', () => {
  it('selectCurrentAppId', () => {
    expect(selectCurrentAppId.projector({ selectedAppId: 'someId' })).toBe('someId');
  });
  it('selectCurrentApp', () => {
    interface AppsInterface {
      entities: Array<Application>;
    }

    const apps: AppsInterface = {
      entities: [
        {
          id: '1',
          name: 'name1',
          owner: { firstName: '', lastName: '', userId: '' },
          role: '',
        },
        {
          id: '2',
          name: 'name2',
          owner: { firstName: '', lastName: '', userId: '' },
          role: '',
        },
      ],
    };
    expect(selectCurrentApp.projector(apps, 0)).toEqual(apps.entities[0]);
    expect(selectCurrentApp.projector(apps, 1)).toEqual(apps.entities[1]);
    expect(selectCurrentApp.projector(apps, 3)).toBeFalsy();
  });

  it('SelectUserRollForSelectedApp', () => {
    const app1 = {
      id: 2,
      name: 'name1',
      role: Role.Administrator,
    };

    const app2 = {
      id: 2,
      name: 'name1',
      role: Role.Owner,
    };
    expect(selectUserRollForSelectedApp.projector(app1)).toBe('ADMINISTRATOR');
    expect(selectUserRollForSelectedApp.projector(app2)).toBe('OWNER');
    expect(selectUserRollForSelectedApp.projector(null)).toBeFalsy();
  });
});

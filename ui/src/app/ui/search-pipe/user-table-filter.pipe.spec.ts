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
import { of } from 'rxjs';

import { UserTableFilterPipe } from './user-table-filter.pipe';

describe('User table Pipe', () => {
  let pipe: UserTableFilterPipe;
  const tableData = [
    {
      fullName: 'Tom Sem',
    },
    {
      fullName: 'Tim Alex',
    },
  ];

  beforeEach(() => {
    pipe = new UserTableFilterPipe();
  });

  it('empty search string', done => {
    const response = pipe.transform(tableData, '');
    const tableData$ = of({ ...response });
    tableData$.subscribe(e => {
      expect(response.length).toBe(2);
      expect(e[0].fullName).toBe(tableData[0].fullName);
      done();
    });
  });

  it('search for "To"', done => {
    const response = pipe.transform(tableData, 'To');
    const tableData$ = of({ ...response });
    tableData$.subscribe(e => {
      expect(response.length).toBe(1);
      expect(e[0].fullName).toBe(tableData[0].fullName);
      done();
    });
  });

  it('search for "Toa"', done => {
    const response = pipe.transform(tableData, 'Toa');
    const tableData$ = of({ ...response });
    tableData$.subscribe(e => {
      const data = Object.entries(e);
      expect(data.length).toBe(0);
      done();
    });
  });
});

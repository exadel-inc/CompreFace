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
  const tableData = {
    data: [
      {
        firstName: 'Tom',
        lastName: 'Sem',
      },
      {
        firstName: 'Tim',
        lastName: 'Alex',
      },
    ],
  };

  beforeEach(() => {
    pipe = new UserTableFilterPipe();
  });

  it('empty search string', done => {
    const tableData$ = of({ ...tableData });
    pipe.transform(tableData$, '').subscribe(e => {
      expect(e.data.length).toBe(2);
      expect(e.data[0].firstName).toBe(tableData.data[0].firstName);
      expect(e.data[0].lastName).toBe(tableData.data[0].lastName);
      expect(e.data[1].firstName).toBe(tableData.data[1].firstName);
      expect(e.data[1].lastName).toBe(tableData.data[1].lastName);
      done();
    });
  });

  it('search for "To"', done => {
    const tableData$ = of({ ...tableData });
    pipe.transform(tableData$, 'To').subscribe(e => {
      expect(e.data.length).toBe(1);
      expect(e.data[0].firstName).toBe(tableData.data[0].firstName);
      expect(e.data[0].lastName).toBe(tableData.data[0].lastName);
      done();
    });
  });

  it('search for "Toa"', done => {
    const tableData$ = of({ ...tableData });
    pipe.transform(tableData$, 'Toa').subscribe(e => {
      expect(e.data.length).toBe(0);
      done();
    });
  });
});

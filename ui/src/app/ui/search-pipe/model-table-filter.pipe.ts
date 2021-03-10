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

import { Pipe, PipeTransform } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Pipe({
  name: 'modelTableFilter',
})
export class ModelTableFilterPipe implements PipeTransform {
  transform(value: Observable<any>, search: string): Observable<any> {
    if (!search.trim()) {
      return value;
    }

    return value.pipe(
      map(e => {
        e.data = e.data.filter(row =>
          (row.name.toLocaleLowerCase() + ' ' + row.type.toLocaleLowerCase()).includes(search.toLocaleLowerCase())
        );
        return e;
      })
    );
  }
}

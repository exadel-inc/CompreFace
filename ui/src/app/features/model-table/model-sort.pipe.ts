/*!
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

import { PipeTransform } from '@angular/core';
import { Pipe } from '@angular/core';
import { ServiceTypes } from 'src/app/data/enums/service-types.enum';
import { Model } from 'src/app/data/interfaces/model';

@Pipe({
  name: 'modelSort',
})
export class ModelSortPipe implements PipeTransform {
  transform(modelCollection: Model[]) {
    const recognition = modelCollection
      .filter(model => model.type === ServiceTypes.Recognition)
      .sort((model, next) => model.name.localeCompare(next.name));

    const detection = modelCollection
      .filter(model => model.type === ServiceTypes.Detection)
      .sort((model, next) => model.name.localeCompare(next.name));

    const verification = modelCollection
      .filter(model => model.type === ServiceTypes.Verification)
      .sort((model, next) => model.name.localeCompare(next.name));

    return [...recognition, ...detection, ...verification];
  }
}

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
import { SimpleChanges } from '@angular/core';

export function checkFile(targetClass, functionName: string, descriptor) {
  const source = descriptor.value;
  descriptor.value = function (changes: SimpleChanges) {
    if (changes) return source.call(this, changes);
  };
  return descriptor;
}

export function recalculateCoordinate(inputs: string[]) {
  return function (targetClass, functionName: string, descriptor) {
    const source = descriptor.set;

    descriptor.set = function (changes: SimpleChanges) {
      inputs.forEach(input => {
        if (!!changes[input]) {
        }
      });
      return source.call(this, changes);
    };
    return descriptor;
  };
}

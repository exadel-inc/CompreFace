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

import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { DragNDropService } from 'src/app/features/drag-n-drop/drag-n-drop.service';

import {
    recognizeFace,
    recognizeFaceSuccess,
    recognizeFaceFail
  } from './actions';

@Injectable()
export class TestEffects {
  constructor(
    private actions: Actions,
    private testService: DragNDropService
  ) { }

  @Effect()
  recognizeFace$ = this.actions.pipe(
    ofType(recognizeFace),
    switchMap(action => this.testService.recognize(action.file, action.model).pipe(
      map(model => recognizeFaceSuccess({ model })),
      catchError(error => of(recognizeFaceFail({ error }))),
    )),
  );

}

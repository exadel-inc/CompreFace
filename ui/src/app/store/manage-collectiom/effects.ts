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
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';

import { SnackBarService } from '../../features/snackbar/snackbar.service';
import { CollectionService } from '../../core/collection/collection.service';
import {
  addSubject,
  addSubjectFail,
  addSubjectSuccess,
  loadSubjects,
  loadSubjectsFail,
  loadSubjectsSuccess
} from './action';

@Injectable()
export class CollectionEffects {
  constructor(private actions: Actions, private collectionService: CollectionService, private snackBarService: SnackBarService) {}

  @Effect()
  loadSubjects$ = this.actions.pipe(
    ofType(loadSubjects),
    switchMap(({ apiKey }) =>
      this.collectionService.getSubjectsList(apiKey).pipe(
        map(({ subjects }) => loadSubjectsSuccess({ subjects })),
        catchError(error => of(loadSubjectsFail({ error })))
      )
    )
  );

  @Effect()
  addSubject$ = this.actions.pipe(
    ofType(addSubject),
    switchMap(({name, apiKey}) =>
      this.collectionService.addSubject(name,apiKey).pipe(
        map( ({ subject}) => addSubjectSuccess({ subject })),
        catchError( error => of(addSubjectFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(loadSubjectsFail, addSubjectFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );
}

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
import { catchError, switchMap, tap, withLatestFrom } from 'rxjs/operators';

import { SnackBarService } from '../../features/snackbar/snackbar.service';
import { CollectionService } from '../../core/collection/collection.service';
import {
  addSubject,
  addSubjectFail,
  addSubjectSuccess,
  initSelectedSubject,
  loadSubjects,
  loadSubjectsFail,
  loadSubjectsSuccess,
} from './action';
import { Store } from '@ngrx/store';
import { selectCollectionCurrentSubject, selectCollectionSubjects } from './selectors';

@Injectable()
export class CollectionEffects {
  constructor(
    private actions: Actions,
    private collectionService: CollectionService,
    private snackBarService: SnackBarService,
    private store: Store<any>
  ) {}

  @Effect()
  loadSubjects$ = this.actions.pipe(
    ofType(loadSubjects),
    switchMap(({ apiKey }) =>
      this.collectionService.getSubjectsList(apiKey).pipe(
        switchMap(({ subjects }) => [loadSubjectsSuccess({ subjects }), initSelectedSubject()]),
        catchError(error => of(loadSubjectsFail({ error })))
      )
    )
  );

  @Effect()
  initSelectedSubject$ = this.actions.pipe(
    ofType(initSelectedSubject),
    withLatestFrom(this.store.select(selectCollectionCurrentSubject), this.store.select(selectCollectionSubjects)),
    switchMap(([, subject, subjects]) => (!subject ? [addSubjectSuccess({ subject: subjects[0] })] : []))
  );

  @Effect()
  addSubject$ = this.actions.pipe(
    ofType(addSubject),
    switchMap(({ name, apiKey }) =>
      this.collectionService.addSubject(name, apiKey).pipe(
        switchMap(({ subject }) => [addSubjectSuccess({ subject }), loadSubjects({ apiKey })]),
        catchError(error => of(addSubjectFail({ error })))
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

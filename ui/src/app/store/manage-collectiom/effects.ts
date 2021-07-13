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
  deleteSubject,
  deleteSubjectFail,
  deleteSubjectSuccess,
  editSubject,
  editSubjectFail,
  editSubjectSuccess,
  initSelectedSubject,
  loadSubjects,
  loadSubjectsFail,
  loadSubjectsSuccess,
  selectedSubject,
} from './action';
import { Store } from '@ngrx/store';
import { selectCollectionCurrentSubject, selectCollectionSubjects } from './selectors';
import { CollectionEntityState } from './reducers';

@Injectable()
export class CollectionEffects {
  constructor(
    private actions: Actions,
    private collectionService: CollectionService,
    private snackBarService: SnackBarService,
    private store: Store<CollectionEntityState>
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
  addSubject$ = this.actions.pipe(
    ofType(addSubject),
    switchMap(({ name, apiKey }) =>
      this.collectionService.addSubject(name, apiKey).pipe(
        switchMap(({ subject }) => [addSubjectSuccess(), selectedSubject({ subject }), loadSubjects({ apiKey })]),
        catchError(error => of(addSubjectFail({ error })))
      )
    )
  );

  @Effect()
  editSubject$ = this.actions.pipe(
    ofType(editSubject),
    switchMap(({ name, apiKey, subject }) =>
      this.collectionService.editSubject(name, apiKey, subject).pipe(
        switchMap(() => [editSubjectSuccess(), selectedSubject({ subject: name }), loadSubjects({ apiKey })]),
        catchError(error => of(editSubjectFail({ error })))
      )
    )
  );

  @Effect()
  deleteSubject$ = this.actions.pipe(
    ofType(deleteSubject),
    switchMap(({ apiKey, subject }) =>
      this.collectionService.deleteSubject(apiKey, subject).pipe(
        switchMap(() => [deleteSubjectSuccess(), selectedSubject({ subject: name }), loadSubjects({ apiKey })]),
        catchError(error => of(deleteSubjectFail({ error })))
      )
    )
  );

  @Effect()
  initSelectedSubject$ = this.actions.pipe(
    ofType(initSelectedSubject),
    withLatestFrom(this.store.select(selectCollectionCurrentSubject), this.store.select(selectCollectionSubjects)),
    switchMap(([, subject, subjects]) => (!subject ? [selectedSubject({ subject: subjects[0] })] : []))
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(loadSubjectsFail, addSubjectFail, deleteSubjectFail, editSubjectFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );
}

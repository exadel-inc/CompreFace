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
import { Store } from '@ngrx/store';
import { from, of, Subject } from 'rxjs';
import { catchError, filter, map, mergeMap, switchMap, take, tap, withLatestFrom } from 'rxjs/operators';

import { SnackBarService } from '../../features/snackbar/snackbar.service';
import { CollectionService } from '../../core/collection/collection.service';
import {
  addFileToCollection,
  addSubject,
  addSubjectFail,
  addSubjectSuccess,
  deleteSubject,
  deleteSubjectExample,
  deleteSubjectExampleFail,
  deleteSubjectExampleSuccess,
  deleteSubjectFail,
  deleteSubjectSuccess,
  editSubject,
  editSubjectFail,
  editSubjectSuccess,
  getSubjectExamples,
  getSubjectExamplesFail,
  getSubjectExamplesSuccess,
  initSelectedSubject,
  loadSubjects,
  loadSubjectsFail,
  loadSubjectsSuccess,
  readImageFiles,
  resetSelectedExamples,
  setSelectedSubject,
  setSubjectMode,
  startUploadImageOrder,
  uploadImage,
  uploadImageFail,
  uploadImageSuccess,
} from './action';
import { CollectionEntityState } from './reducers';
import { selectCollectionSubject, selectCollectionSubjects, selectImageCollection } from './selectors';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { selectCurrentApiKey } from '../model/selectors';
import { SubjectModeEnum } from 'src/app/data/enums/subject-mode.enum';

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
        switchMap(({ subject }) => [addSubjectSuccess({ subject }), loadSubjects({ apiKey })]),
        catchError(error => of(addSubjectFail({ error })))
      )
    )
  );

  @Effect()
  editSubject$ = this.actions.pipe(
    ofType(editSubject),
    switchMap(({ editName, apiKey, subject }) =>
      this.collectionService.editSubject(editName, apiKey, subject).pipe(
        switchMap(() => [editSubjectSuccess({ subject: editName }), loadSubjects({ apiKey })]),
        catchError(error => of(editSubjectFail({ error })))
      )
    )
  );

  @Effect()
  deleteSubject$ = this.actions.pipe(
    ofType(deleteSubject),
    switchMap(({ name, apiKey }) =>
      this.collectionService.deleteSubject(name, apiKey).pipe(
        switchMap(() => [deleteSubjectSuccess(), loadSubjects({ apiKey })]),
        catchError(error => of(deleteSubjectFail({ error })))
      )
    )
  );

  @Effect()
  initSelectedSubject$ = this.actions.pipe(
    ofType(initSelectedSubject),
    withLatestFrom(this.store.select(selectCollectionSubject), this.store.select(selectCollectionSubjects)),
    switchMap(([, subject, subjects]) => (!subject ? [setSelectedSubject({ subject: subjects[0] })] : []))
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(loadSubjectsFail, addSubjectFail, editSubjectFail, deleteSubjectFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );

  @Effect()
  loadSubjectExamples$ = this.actions.pipe(
    ofType(getSubjectExamples),
    withLatestFrom(this.store.select(selectCurrentApiKey)),
    switchMap(([, apiKey]) =>
      this.collectionService.getSubjectExampleList(apiKey).pipe(
        map(items => getSubjectExamplesSuccess({ items, apiKey })),
        catchError(error => of(getSubjectExamplesFail({ error })))
      )
    )
  );

  @Effect()
  deleteSubjectExample$ = this.actions.pipe(
    ofType(deleteSubjectExample),
    withLatestFrom(this.store.select(selectCurrentApiKey)),
    switchMap(([{ item }, apiKey]) =>
      this.collectionService.deleteSubjectExample(item, apiKey).pipe(
        switchMap(() => [deleteSubjectExampleSuccess({ item }), getSubjectExamples()]),
        catchError(error => of(deleteSubjectExampleFail({ item, error })))
      )
    )
  );

  @Effect()
  readImageFile$ = this.actions.pipe(
    ofType(readImageFiles),
    withLatestFrom(this.store.select(selectCollectionSubject)),
    switchMap(([action, subject]) => {
      return from(action.fileDescriptors).pipe(
        mergeMap(file => {
          const fileReader = new FileReader();
          const resSubject = new Subject<{ file: File; url: string; subject: string }>();

          fileReader.onload = e =>
            resSubject.next({
              url: e.target.result as string,
              file,
              subject,
            });
          fileReader.readAsDataURL(file);

          return resSubject.asObservable();
        }),
        switchMap(payload => [addFileToCollection(payload), startUploadImageOrder()])
      );
    })
  );

  @Effect()
  startUploadOrder$ = this.actions.pipe(
    ofType(startUploadImageOrder),
    switchMap(() =>
      this.store.select(selectImageCollection).pipe(
        take(1),
        filter(collection => !collection.some(item => item.status === CircleLoadingProgressEnum.InProgress)),
        map(collection => collection.find(item => item.status === CircleLoadingProgressEnum.OnHold))
      )
    ),
    map(item => {
      if (item) {
        return uploadImage({ item, continueUpload: true });
      }

      return getSubjectExamples();
    })
  );

  @Effect()
  uploadImage$ = this.actions.pipe(
    ofType(uploadImage),
    withLatestFrom(this.store.select(selectCurrentApiKey), this.store.select(selectCollectionSubject)),
    switchMap(([{ item, continueUpload }, apiKey, subject]) => {
      return this.collectionService.uploadSubjectExamples(item, subject, apiKey).pipe(
        map(() => uploadImageSuccess({ item, continueUpload })),
        catchError(error => {
          return of(uploadImageFail({ error: error.error?.message, item, continueUpload }));
        })
      );
    })
  );

  @Effect({ dispatch: false })
  uploadImageSuccess$ = this.actions.pipe(
    ofType(uploadImageSuccess, uploadImageFail),
    tap(({ continueUpload }) => {
      if (continueUpload) {
        this.store.dispatch(startUploadImageOrder());
      }
    })
  );

  @Effect()
  setSubjectMode$ = this.actions.pipe(
    ofType(setSubjectMode),
    switchMap(({ mode }) => (mode === SubjectModeEnum.Default ? [resetSelectedExamples()] : []))
  );

  @Effect()
  setSelectedSubject$ = this.actions.pipe(
    ofType(setSelectedSubject),
    switchMap(() => [setSubjectMode({ mode: SubjectModeEnum.Default })])
  );
}

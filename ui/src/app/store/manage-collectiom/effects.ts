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
  deleteSelectedExamples,
  deleteSelectedExamplesFail,
  deleteSelectedExamplesSuccess,
  deleteSubject,
  deleteSubjectExample,
  deleteSubjectExampleFail,
  deleteSubjectExampleSuccess,
  deleteSubjectFail,
  deleteSubjectSuccess,
  editSubject,
  editSubjectFail,
  editSubjectSuccess,
  getNextPageSubjectExamplesSuccess,
  getSubjectExamples,
  getSubjectExamplesFail,
  getSubjectExamplesSuccess,
  getSubjectMediaNextPage,
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
import { CollectionItem } from 'src/app/data/interfaces/collection';
import { selectMaxFileSize } from '../image-size/selectors';
import { Routes } from 'src/app/data/enums/routers-url.enum';
import { Router } from '@angular/router';

@Injectable()
export class CollectionEffects {
  constructor(
    private actions: Actions,
    private collectionService: CollectionService,
    private snackBarService: SnackBarService,
    private router: Router,
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
    ofType(loadSubjectsFail, addSubjectFail, editSubjectFail, deleteSubjectFail, deleteSelectedExamplesFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error);
    })
  );

  @Effect()
  loadSubjectExamples$ = this.actions.pipe(
    ofType(getSubjectExamples),
    withLatestFrom(this.store.select(selectCurrentApiKey)),
    switchMap(([{ subject }, apiKey]) =>
      this.collectionService.getSubjectMedia(apiKey, subject).pipe(
        map(items => getSubjectExamplesSuccess({ items, apiKey })),
        catchError(error => of(getSubjectExamplesFail({ error })))
      )
    )
  );

  @Effect()
  loadNextPage$ = this.actions.pipe(
    ofType(getSubjectMediaNextPage),
    withLatestFrom(this.store.select(selectCurrentApiKey)),
    switchMap(([{ subject, page, totalPages }, apiKey]) => {
      const nextPage = page + 1;
      if (nextPage !== totalPages) {
        return this.collectionService.getSubjectMediaNextPage(apiKey, subject, nextPage).pipe(
          map(items => getNextPageSubjectExamplesSuccess({ items, apiKey })),
          catchError(error => of(getSubjectExamplesFail({ error })))
        );
      }
    })
  );

  @Effect()
  deleteSubjectExample$ = this.actions.pipe(
    ofType(deleteSubjectExample),
    withLatestFrom(this.store.select(selectCurrentApiKey)),
    switchMap(([{ item }, apiKey]) =>
      this.collectionService.deleteSubjectExample(item, apiKey).pipe(
        map(item => item.subject),
        switchMap(subject => [deleteSubjectExampleSuccess({ item }), getSubjectExamples({ subject })]),
        catchError(error => of(deleteSubjectExampleFail({ item, error })))
      )
    )
  );

  @Effect()
  readImageFile$ = this.actions.pipe(
    ofType(readImageFiles),
    withLatestFrom(this.store.select(selectCollectionSubject)),
    switchMap(([action, subject]) => {
      const fileUrls = [];

      return from(action.fileDescriptors).pipe(
        mergeMap(file => {
          const fileReader = new FileReader();
          const resSubject = new Subject<{ file: File; url: string; subject: string }>();

          fileReader.onload = e => {
            const url = e.target.result as string;

            if (fileUrls.indexOf(url) !== -1) return;

            resSubject.next({
              url: url,
              file,
              subject,
            });

            fileUrls.push(url);
          };
          fileReader.readAsDataURL(file);

          return resSubject.asObservable();
        }),
        switchMap(payload => [addFileToCollection(payload), startUploadImageOrder()])
      );
    })
  );

  collectionItem: CollectionItem;
  currentSubject: string;

  @Effect()
  startUploadOrder$ = this.actions.pipe(
    ofType(startUploadImageOrder),
    withLatestFrom(this.store.select(selectCollectionSubject)),
    tap(([, subject]) => (this.currentSubject = subject)),
    switchMap(() =>
      this.store.select(selectImageCollection).pipe(
        take(1),
        filter(collection => !collection.some(item => item.status === CircleLoadingProgressEnum.InProgress)),
        map(collection => collection.find(item => item.status === CircleLoadingProgressEnum.OnHold))
      )
    ),
    map(item => {
      if (item && this.currentSubject === item.subject) {
        this.collectionItem = item;
        return uploadImage({ item, continueUpload: true });
      }

      let subject = this.currentSubject;
      return getSubjectExamples({ subject });
    })
  );

  @Effect()
  uploadImage$ = this.actions.pipe(
    ofType(uploadImage),
    withLatestFrom(
      this.store.select(selectCurrentApiKey),
      this.store.select(selectCollectionSubject),
      this.store.select(selectMaxFileSize)
    ),
    switchMap(([{ item, continueUpload }, apiKey, subject, maxFileSize]) => {
      const { file } = item;
      const sizeInBytes = maxFileSize.clientMaxFileSize;
      const ext = /(\.jpg|\.jpeg|\.webp|\.png)$/i;
      const type = /(\/jpg|\/jpeg|\/webp|\/png)$/i;

      if (sizeInBytes && file.size > sizeInBytes) {
        return of(uploadImageFail({ error: `Invalid File Size ! \n File Name: ${file.name}`, item, continueUpload }));
      }
      if (!ext.exec(file.name) || !type.exec(file.type)) {
        return of(uploadImageFail({ error: `Invalid File Type ! \n File Name: ${file.name}`, item, continueUpload }));
      }

      return this.collectionService.uploadSubjectExamples(item, subject, apiKey).pipe(
        map(res => {
          const itemId = res.image_id;
          return uploadImageSuccess({ item, itemId, continueUpload });
        }),
        catchError(error => {
          return of(uploadImageFail({ error: error.error?.message + `! \n File Name: ${item.file.name}`, item, continueUpload }));
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

  @Effect()
  deleteSelectedExamples$ = this.actions.pipe(
    ofType(deleteSelectedExamples),
    withLatestFrom(this.store.select(selectCurrentApiKey)),
    switchMap(([{ ids }, apiKey]) =>
      this.collectionService.deleteSubjectExamplesBulk(ids, apiKey).pipe(
        map(item => item[0].subject),
        switchMap(subject => [deleteSelectedExamplesSuccess(), getSubjectExamples({ subject })]),
        catchError(error => of(deleteSelectedExamplesFail({ error })))
      )
    )
  );

  //Listen for the 'loadModelsFail'
  @Effect({ dispatch: false })
  loadFail$ = this.actions.pipe(
    ofType(loadSubjectsFail),
    tap(() => {
      this.router.navigateByUrl(Routes.Home);
    })
  );
}

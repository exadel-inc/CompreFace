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
import { Action, Store } from '@ngrx/store';

import { iif, Observable, of } from 'rxjs';
import { catchError, map, switchMap, tap, withLatestFrom } from 'rxjs/operators';

import { SnackBarService } from 'src/app/features/snackbar/snackbar.service';
import { FaceRecognitionService } from '../../core/face-recognition/face-recognition.service';
import { selectDemoApiKey } from '../demo/selectors';
import { selectCurrentModel } from '../model/selectors';
import { selectFiles } from './selectors';
import { verifyFace, verifyFaceSuccess, verifyFaceFail, verifyFaceAddProcessFile, verifyFaceAddCheckFileFile } from './action';

@Injectable()
export class FaceRecognitionEffects {
  constructor(
    private actions: Actions,
    private store: Store<any>,
    private recognitionService: FaceRecognitionService,
    private snackBarService: SnackBarService
  ) {}

  @Effect()
  verifyFaceAddFile$ = this.actions.pipe(
    ofType(verifyFaceAddProcessFile, verifyFaceAddCheckFileFile),
    withLatestFrom(this.store.select(selectFiles)),
    switchMap(([, files]) => (files.processFile && files.checkFile ? [verifyFace()] : []))
  );

  @Effect()
  verifyFaceSaveToStore$ = this.actions.pipe(
    ofType(verifyFace),
    withLatestFrom(this.store.select(selectCurrentModel), this.store.select(selectDemoApiKey), this.store.select(selectFiles)),
    switchMap(([, model, demoApiKey, files]) =>
      iif(
        () => !!model,
        this.verificationFace(files.processFile, files.checkFile, model?.apiKey),
        this.verificationFace(files.processFile, files.checkFile, demoApiKey)
      )
    )
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(verifyFaceFail),
    tap(action => this.snackBarService.openHttpError(action.error))
  );

  private verificationFace(processFile, checkFile, apiKey): Observable<Action> {
    return this.recognitionService.verification(processFile, checkFile, apiKey).pipe(
      map(({ data, request }) =>
        verifyFaceSuccess({
          model: data,
          request,
        })
      ),
      catchError(error => of(verifyFaceFail({ error })))
    );
  }
}

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
import { addFace, addFaceFail, addFaceSuccess, recognizeFace, recognizeFaceFail, recognizeFaceSuccess } from './actions';

@Injectable()
export class FaceRecognitionEffects {
  constructor(
    private actions: Actions,
    private store: Store<any>,
    private recognitionService: FaceRecognitionService,
    private snackBarService: SnackBarService
  ) {}

  @Effect()
  recognizeFace$ = this.actions.pipe(
    ofType(recognizeFace),
    withLatestFrom(this.store.select(selectCurrentModel), this.store.select(selectDemoApiKey)),
    switchMap(([action, model, demoApiKey]) =>
      iif(() => !!model, this.recognizeFace(action.file, model?.apiKey), this.recognizeFace(action.file, demoApiKey))
    )
  );

  @Effect()
  addFace$ = this.actions.pipe(
    ofType(addFace),
    switchMap(action =>
      this.recognitionService.addFace(action.file, action.model).pipe(
        map(model => addFaceSuccess({ model })),
        catchError(error => of(addFaceFail({ error })))
      )
    )
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(recognizeFaceFail, addFaceFail),
    tap(action => {
      this.snackBarService.openHttpError(action.error.message);
    })
  );

  @Effect({ dispatch: false })
  recognizeFaceSuccess$: Observable<any> = this.actions.pipe(
    ofType(recognizeFaceSuccess),
    tap(action => {
      if (action.model.result.length === 0) {
        this.snackBarService.openNotification({ messageText: 'face_recognition.no_recognized', type: 'warning' });
      } else if (action.model.result[0].faces.length === 0) {
        this.snackBarService.openNotification({ messageText: 'face_recognition.no_faces_in_collection', type: 'error' });
      }
    })
  );

  /**
   * Method made to finish recognize face effect, and for better understanding (more readable code).
   *
   * @param file Image
   * @param apiKey model api key
   */
  private recognizeFace(file, apiKey): Observable<Action> {
    return this.recognitionService.recognize(file, apiKey).pipe(
      map(({ data, request }) =>
        recognizeFaceSuccess({
          model: data,
          file,
          request,
        })
      ),
      catchError(error => of(recognizeFaceFail({ error })))
    );
  }
}

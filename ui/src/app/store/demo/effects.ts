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
import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { DemoService } from '../../pages/demo/demo.service';
import { loadDemoApiKey, loadDemoApiKeyFail, loadDemoApiKeySuccess, loadDemoStatus } from './action';

@Injectable()
export class DemoEffects {
  constructor(private actions: Actions, private demoService: DemoService) {}

  @Effect()
  loadDemoStatus$ = this.actions.pipe(
    ofType(loadDemoStatus),
    switchMap(() =>
      this.demoService.getStatus().pipe(
        map(data => {
          if (!data.demoFaceCollectionIsInconsistent) {
            return loadDemoApiKey();
          }
          return loadDemoApiKeyFail();
        })
      )
    )
  );

  @Effect()
  loadDemoApiKey$ = this.actions.pipe(
    ofType(loadDemoApiKey),
    switchMap(() =>
      this.demoService.getModel().pipe(
        map(data => loadDemoApiKeySuccess(data)),
        catchError((response: any): Observable<any> => {
          if (response instanceof HttpErrorResponse) {
            return of(loadDemoApiKeyFail());
          }

          return throwError(response);
        })
      )
    )
  );
}

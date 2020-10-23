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

import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DemoPageService {

  constructor(private http: HttpClient) { }

  getDemoPageStatus(): Observable<any> {
    return this.http.get(`${environment.adminApiUrl}user/demo/model`).pipe(
      map(() => of(true)),
      catchError((response: any): Observable<boolean> => {
        if (response instanceof HttpErrorResponse && response.status === 403) {
          return of(false);
        }

        return throwError(response);
      })
    );
  }
}

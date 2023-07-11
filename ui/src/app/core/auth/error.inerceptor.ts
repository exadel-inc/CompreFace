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
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, take, tap } from 'rxjs/operators';

import { AuthService } from './auth.service';
import { Store } from '@ngrx/store';
import { AppState } from 'src/app/store';
import { getBeServerStatus, getCoreServerStatus, getDbServerStatus } from 'src/app/store/servers-status/actions';
import { UserInfoResolver } from '../user-info/user-info.resolver';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private authService: AuthService;

  constructor(private injector: Injector, private store: Store<AppState>) {
    this.authService = this.injector.get(AuthService);
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((response: any): Observable<HttpEvent<any>> => {
        if (response instanceof HttpErrorResponse) {
          if (response.status === 401) {
            this.authService.logOut();
          } else if (response.status === 502) {
            this.authService.currentUserId$.pipe(
              take(1),
              tap(userId => {
                if (userId) {
                  this.store.dispatch(getBeServerStatus());
                  this.store.dispatch(getDbServerStatus());
                  this.store.dispatch(getCoreServerStatus());
                } else {
                  this.authService.navigateToLogin();
                }
              })
            ).subscribe();
          }
        }
        return throwError(response);
      })
    );
  }
}

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
import { Observable, combineLatest, throwError } from 'rxjs';
import { catchError, filter, take, tap } from 'rxjs/operators';

import { AuthService } from './auth.service';
import { Store } from '@ngrx/store';
import { AppState } from 'src/app/store';
import { getBeServerStatus, getCoreServerStatus, getDbServerStatus } from 'src/app/store/servers-status/actions';
import { ServerStatusInt } from 'src/app/store/servers-status/reducers';
import { selectServerStatus } from 'src/app/store/servers-status/selectors';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private authService: AuthService;
  serverStatus$: Observable<ServerStatusInt>;

  constructor(private injector: Injector, private store: Store<AppState>) {
    this.authService = this.injector.get(AuthService);
    this.serverStatus$ = this.store.select(selectServerStatus).pipe(filter(status => !!status));
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((response: any): Observable<HttpEvent<any>> => {
        if (response instanceof HttpErrorResponse) {
          if (response.status === 401) {
            this.authService.logOut();
          } else if (response.status === 502) {
            combineLatest([this.authService.currentUserId$, this.serverStatus$])
              .pipe(
                take(1),
                tap(([userId, statuses]) => {
                  const { status, apiStatus, coreStatus } = statuses;
                  if (!userId) {
                    this.authService.navigateToLogin();
                  } else {
                    this.updateServerStatus(apiStatus, status, coreStatus);
                  }
                })
              )
              .subscribe();
          }
        }
        return throwError(response);
      })
    );
  }

  private updateServerStatus(apiStatus: string, status: string, coreStatus: string): void {
    const preserveState = !(apiStatus && status && coreStatus);
    this.store.dispatch(getBeServerStatus({preserveState}));
    this.store.dispatch(getDbServerStatus({preserveState}));
    this.store.dispatch(getCoreServerStatus({preserveState}));
  }
}

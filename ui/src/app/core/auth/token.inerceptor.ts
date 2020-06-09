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

import { Injectable, Injector } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { API_URL } from '../../data/api.variables';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  private authService: AuthService;

  constructor(private injector: Injector) {
    this.authService = this.injector.get(AuthService);
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    if (token && !request.url.includes(API_URL.REFRESH_TOKEN)) {
      request = request.clone({
        setHeaders: {
          Authorization: token,
        }
      });
    }

    return next.handle(request);
  }
}

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private authService: AuthService;

  constructor(private injector: Injector) {
    this.authService = this.injector.get(AuthService);
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    return next.handle(request).pipe(
      catchError((response: any): Observable<HttpEvent<any>> => {
        if (response instanceof HttpErrorResponse && response.status === 401) {
          if (response.error.error && response.error.error === 'invalid_token') {
            return this.authService.refreshToken(request);
          } else {
            this.authService.logOut();
          }
        }

        return throwError(response);
      })
    );
  }
}

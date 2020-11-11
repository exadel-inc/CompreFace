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
import { Router } from '@angular/router';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of as observableOf } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { SnackBarService } from 'src/app/features/snackbar/snackbar.service';

import { AuthService } from '../../core/auth/auth.service';
import { ROUTERS_URL } from '../../data/enums/routers-url.enum';
import { resetUserInfo } from '../userInfo/action';
import {
  clearUserToken,
  logIn,
  logInFailure,
  logInSuccess,
  logOut,
  signUp,
  signUpFailure,
  signUpSuccess
} from './action';

@Injectable()
export class AuthEffects {
  constructor(
    private actions: Actions,
    private authService: AuthService,
    private router: Router,
    private snackBarService: SnackBarService,
  ) { }

  // Listen for the 'LOGIN' action
  @Effect()
  LogIn = this.actions.pipe(
    ofType(logIn),
    switchMap(action => {
      return this.authService.logIn(action.email, action.password).pipe(
        map(() => logInSuccess()),
        catchError(error => observableOf(logInFailure(error)))
      );
    }));

  // Listen for the 'LogInSuccess' action
  @Effect({ dispatch: false })
  LogInSuccess: Observable<any> = this.actions.pipe(
    ofType(logInSuccess),
    tap(() => {
      this.router.navigateByUrl(ROUTERS_URL.HOME);
    })
  );

  @Effect({ dispatch: false })
  showError$ = this.actions.pipe(
    ofType(logInFailure, signUpFailure),
    tap(action => {
      if (action.error && action.error.error_description === 'Bad credentials') {
        this.snackBarService.openError('auth.incorrect_credentials');
      } else if (action.error && action.error.code === 4) {
        this.snackBarService.openError('auth.already_in_use');
      } else {
        this.snackBarService.openHttpError(action.error);
      }
    })
  );

  @Effect({ dispatch: false })
  showSuccess$ = this.actions.pipe(
    ofType(signUpSuccess),
    tap(action => {
      const message = action.confirmationNeeded
        ? 'auth.new_account_confirm_email'
        : 'auth.new_account_login';
      this.snackBarService.openInfo(message);
    }),
  );

  @Effect()
  SignUp: Observable<any> = this.actions.pipe(
    ofType(signUp),
    switchMap(payload => {
      return this.authService.signUp(payload.firstName, payload.password, payload.email, payload.lastName).pipe(
        map(res => signUpSuccess({ confirmationNeeded: res.status === 200 })),
        catchError(error => observableOf(signUpFailure(error)))
      );
    }));

  @Effect({ dispatch: false })
  SignUpSuccess: Observable<any> = this.actions.pipe(
    ofType(signUpSuccess),
    tap(() => {
      this.router.navigateByUrl(ROUTERS_URL.LOGIN);
    })
  );

  @Effect({ dispatch: false })
  SignUpFailure: Observable<any> = this.actions.pipe(
    ofType(signUpFailure)
  );

  @Effect()
  public LogOut: Observable<any> = this.actions.pipe(
    ofType(logOut),
    switchMap(() => {
      this.router.navigateByUrl(ROUTERS_URL.LOGIN);

      return [clearUserToken(), resetUserInfo()];
    })
  );

  @Effect({dispatch: false})
  public ClearUserToken: Observable<any> = this.actions.pipe(
    ofType(clearUserToken),
    switchMap(() => this.authService.clearUserToken())
  );
}

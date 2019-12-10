import { Injectable } from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import { Observable, of as observableOf } from 'rxjs';
import {AuthService} from "../../core/auth/auth.service";
import {AuthActionTypes, LogInSuccess, LogInFailure, SignUpFailure, SignUpSuccess, LogOut} from "../actions/auth";
import {catchError, map, switchMap, tap} from "rxjs/operators";
import { LogIn } from '../actions/auth';
import {Router} from "@angular/router";
import {ROUTERS_URL} from "../../data/routers-url";

@Injectable()
export class AuthEffects {
  constructor(private actions: Actions, private authService: AuthService, private router: Router) {}

  // Listen for the 'LOGIN' action
  @Effect()
  LogIn: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.LOGIN),
    map((action: LogIn) => action.payload),
    switchMap(payload => {
      return this.authService.logIn(payload.username, payload.password).pipe(
        map((user) => {
          console.log(user);
          return new LogInSuccess({token: user.token, email: payload.email});
        }),
        catchError(error =>
          observableOf(new LogInFailure({ error }))
        )
      )

    }));

  // Listen for the 'LogInSuccess' action
  @Effect({ dispatch: false })
  LogInSuccess: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.LOGIN_SUCCESS),
    tap((user) => {
      localStorage.setItem('token', user.payload.token);
      this.router.navigateByUrl(ROUTERS_URL.ORGANIZATION);
    })
  );

  // Listen for the 'LogInFailure' action
  @Effect({ dispatch: false })
  LogInFailure: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.LOGIN_FAILURE)
  );

  @Effect()
  SignUp: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.SIGNUP),
    map((action: LogIn) => action.payload),
    switchMap(payload => {
      return this.authService.signUp(payload.username, payload.password, payload.email).pipe(
        map(() => {
          return new SignUpSuccess({});
        }),
        catchError(error =>
          observableOf(new SignUpFailure({ error }))
        )
      )

    }));

  @Effect({ dispatch: false })
  SignUpSuccess: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.SIGNUP_SUCCESS),
    tap(() => {
      this.router.navigateByUrl(ROUTERS_URL.LOGIN);
    })
  );

  @Effect({ dispatch: false })
  SignUpFailure: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.SIGNUP_FAILURE)
  );

  @Effect({ dispatch: false })
  public LogOut: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.LOGOUT),
    tap((user) => {
      localStorage.removeItem('token');
      this.router.navigateByUrl(ROUTERS_URL.LOGIN);
    })
  );
}

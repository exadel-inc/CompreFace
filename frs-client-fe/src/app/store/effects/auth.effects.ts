import { Injectable } from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import { Observable, of as observableOf } from 'rxjs';
import {AuthService} from "../../core/auth/auth.service";
import {AuthActionTypes, LogInSuccess, LogInFailure} from "../actions/auth";
import {catchError, map, switchMap, tap} from "rxjs/operators";
import { LogIn } from '../actions/auth';
import {Router} from "@angular/router";

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

  @Effect({ dispatch: false })
  LogInSuccess: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.LOGIN_SUCCESS),
    tap((user) => {
      localStorage.setItem('token', user.payload.token);
      this.router.navigateByUrl('/');
    })
  );

  @Effect({ dispatch: false })
  LogInFailure: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.LOGIN_FAILURE)
  );
}

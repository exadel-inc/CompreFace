import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Observable, of as observableOf} from 'rxjs';
import {AuthService} from '../../core/auth/auth.service';
import {AuthActionTypes, LogInSuccess, LogInFailure, SignUpFailure, SignUpSuccess, LogIn, SignUp} from './action';
import {UpdateUserInfo, ResetUserInfo} from '../userInfo/action';
import {catchError, map, switchMap, tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import {ROUTERS_URL} from '../../data/routers-url.variable';

@Injectable()
export class AuthEffects {
  constructor(private actions: Actions, private authService: AuthService, private router: Router) { }

  // Listen for the 'LOGIN' action
  @Effect()
  LogIn: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.LOGIN),
    map((action: LogIn) => action.payload),
    switchMap(payload => {
      return this.authService.logIn(payload.username, payload.password).pipe(
        switchMap(res => {
          this.authService.updateToken(res.token);
          return [
            new LogInSuccess(),
            new UpdateUserInfo(
              {
                isAuthenticated: true,
                username: payload.username
              })
          ];
        }),
        catchError(error =>
          observableOf(new LogInFailure({ error }))
        )
      );
    }));

  // Listen for the 'LogInSuccess' action
  @Effect({ dispatch: false })
  LogInSuccess: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.LOGIN_SUCCESS),
    tap(() => {
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
    map((action: SignUp) => action.payload),
    switchMap(payload => {
      return this.authService.signUp(payload.username, payload.password, payload.email).pipe(
        map(() => new SignUpSuccess()),
        catchError(error =>
          observableOf(new SignUpFailure({ error }))
        )
      );
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

  @Effect()
  public LogOut: Observable<any> = this.actions.pipe(
    ofType(AuthActionTypes.LOGOUT),
    map(() => {
      this.authService.removeToken();
      this.router.navigateByUrl(ROUTERS_URL.LOGIN);
      return new ResetUserInfo();
    })
  );
}

import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Observable, of as observableOf} from 'rxjs';
import {AuthService} from '../../core/auth/auth.service';
import {
  logInSuccess,
  logInFailure,
  signUpFailure,
  signUpSuccess,
  logIn,
  signUp,
  logOut
} from './action';
import {updateUserInfo, resetUserInfo} from '../userInfo/action';
import {catchError, map, switchMap, tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import {ROUTERS_URL} from '../../data/routers-url.variable';

@Injectable()
export class AuthEffects {
  constructor(private actions: Actions, private authService: AuthService, private router: Router) { }

  // Listen for the 'LOGIN' action
  @Effect()
  LogIn = this.actions.pipe(
    ofType(logIn),
    switchMap(action => {
      return this.authService.logIn(action.email, action.password).pipe(
        switchMap(res => {
          this.authService.updateToken(res.token);
          return [
            logInSuccess(),
            updateUserInfo(
              {
                isAuthenticated: true,
                firstName: res.firstName
              })
          ];
        }),
        catchError(() => observableOf(logInFailure()))
      );
    }));

  // Listen for the 'LogInSuccess' action
  @Effect({ dispatch: false })
  LogInSuccess: Observable<any> = this.actions.pipe(
    ofType(logInSuccess),
    tap(() => {
      this.router.navigateByUrl(ROUTERS_URL.ORGANIZATION);
    })
  );

  // Listen for the 'LogInFailure' action
  @Effect({ dispatch: false })
  LogInFailure: Observable<any> = this.actions.pipe(
    ofType(logInFailure)
  );

  @Effect()
  SignUp: Observable<any> = this.actions.pipe(
    ofType(signUp),
    switchMap(payload => {
      return this.authService.signUp(payload.firstName, payload.password, payload.email, payload.lastName).pipe(
        map(() => signUpSuccess()),
        catchError(() => observableOf(signUpFailure()))
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
    map(() => {
      this.authService.removeToken();
      this.router.navigateByUrl(ROUTERS_URL.LOGIN);
      return resetUserInfo();
    })
  );
}

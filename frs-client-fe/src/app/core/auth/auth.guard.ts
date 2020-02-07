import {Injectable} from '@angular/core';
import {Router, CanActivate} from '@angular/router';
import {Store} from '@ngrx/store';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {AppState} from 'src/app/store';
import {selectUserInfoState} from '../../store/userInfo/selectors';
import {Observable} from 'rxjs';
import {take, map} from 'rxjs/operators';
import {UserInfoState} from 'src/app/store/userInfo/reducers';

@Injectable()
export class AuthGuard implements CanActivate {
  private userInfo: Observable<UserInfoState>;

  constructor(public router: Router, private store: Store<AppState>) {
    this.userInfo = this.store.select(selectUserInfoState);
  }

  canActivate(): Observable<boolean> {
    return this.userInfo.pipe(
      take(1),
      map((state: UserInfoState) => {
        if (!state.isAuthenticated) {
          this.router.navigateByUrl(ROUTERS_URL.LOGIN);
        }

        return !!state.isAuthenticated;
      })
    );
  }
}

@Injectable()
export class LoginGuard implements CanActivate {
  private userInfo: Observable<UserInfoState>;

  constructor(public router: Router, private store: Store<AppState>) {
    this.userInfo = this.store.select(selectUserInfoState);
  }

  canActivate(): Observable<boolean> {
    return this.userInfo.pipe(
      take(1),
      map((state: UserInfoState) => {
        if (state.isAuthenticated) {
          this.router.navigateByUrl(ROUTERS_URL.ORGANIZATION);
        }

        return !state.isAuthenticated;
      })
    );
  }
}

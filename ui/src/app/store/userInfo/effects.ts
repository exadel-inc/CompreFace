import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {of} from 'rxjs';
import {switchMap, catchError} from 'rxjs/operators';
import {
  getUserInfo,
  getUserInfoFail,
  getUserInfoSuccess
} from './action';
import {UserInfoService} from '../../core/user-info/user-info.service';

@Injectable()
export class UserInfoEffect {
  constructor(private actions: Actions, private userInfoService: UserInfoService) { }


  @Effect()
  getUser$ = this.actions.pipe(
    ofType(getUserInfo),
    switchMap(() => {
      return this.userInfoService.get().pipe(
        switchMap(user => [getUserInfoSuccess({ user })]),
        catchError(e => of(getUserInfoFail({ errorMessage: e })))
      );
    })
  );
}

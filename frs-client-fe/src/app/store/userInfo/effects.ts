import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Observable, of} from 'rxjs';
import {switchMap, catchError} from 'rxjs/operators';
import {
  GetUserInfoFail, GetUserInfoSuccess, UserInfoActionTypes
} from './action';
import {UserInfoService} from '../../core/user-info/user-info.service';

@Injectable()
export class UserInfoEffect {
  constructor(private actions: Actions, private userInfoService: UserInfoService) { }


  @Effect()
  getUser$: Observable<GetUserInfoSuccess | GetUserInfoFail> = this.actions.pipe(
    ofType(UserInfoActionTypes.GET_USER_INFO),
    switchMap(() => {
      return this.userInfoService.get().pipe(
        switchMap(user => [new GetUserInfoSuccess(user)]),
        catchError(e => of(new GetUserInfoFail({ errorMessage: e })))
      );
    })
  );
}

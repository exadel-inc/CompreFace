import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import {
  GetUserInfo, UserInfoActionTypes
} from './action';
import { Store } from '@ngrx/store';
import { AppState } from '..';
import {UserInfoService} from "../../core/user-info/user-info.service";

@Injectable()
export class UserInfoEffect {
  constructor(private actions: Actions, private userInfoService: UserInfoService, private store: Store<AppState>) { }


  // @Effect()
  // fetchApplicationList: Observable<FetchApplicationListSuccess | AddApplicationsEntityAction | FetchApplicationListFail> = this.actions.pipe(
  //   ofType(UserInfoActionTypes.GET_USER_INFO),
  //   switchMap((action: GetUserInfo) => {
  //     return this.userInfoService.get().pipe(
  //       switchMap(user =>
  //         [
  //           // new FetchApplicationListSuccess(),
  //           // new AddApplicationsEntityAction({ applications: apps })
  //         ]),
  //       catchError(e => of(new FetchApplicationListFail({ errorMessage: e })))
  //     )
  //   })
  // )

  // @Effect()
  // createApplication: Observable<CreateApplicationSuccess | AddApplicationEntityAction | CreateApplicationFail> = this.actions.pipe(
  //   ofType(ApplicationListTypes.CREATE_APPLICATION),
  //   switchMap((action: CreateApplication) => {
  //     return this.applicationService.create(action.payload.organizationId, action.payload.name)
  //       .pipe(
  //         switchMap((app) => [
  //           new CreateApplicationSuccess(),
  //           new AddApplicationEntityAction({ application: app })
  //         ]),
  //         catchError(error => of(new CreateApplicationFail({ errorMessage: error })))
  //       )
  //   })
  // )
}

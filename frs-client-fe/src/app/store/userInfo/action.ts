import {Action} from '@ngrx/store';
import {User} from "../../data/user";

export enum UserInfoActionTypes {
  UPDATE_USER_INFO = '[User] Login Success',
  RESET_USER_INFO = '[User] Update Token',
  UPDATE_AUTHORIZED = '[User] Toggle Authorized',
  GET_USER_INFO = '[User] Get User Info',
  GET_USER_INFO_SUCCESS = '[User] Get User Info Success',
  GET_USER_INFO_FAIL = '[User] Get User Info Fail'
}

export class UpdateUserInfo implements Action {
  readonly type = UserInfoActionTypes.UPDATE_USER_INFO;
  constructor(public payload: {
    isAuthenticated?: boolean,
    username?: string
  }) {}
}

export class ResetUserInfo implements Action {
  readonly type = UserInfoActionTypes.RESET_USER_INFO;
}

export class UpdateUserAuthorization implements Action {
  readonly type = UserInfoActionTypes.UPDATE_AUTHORIZED;
  constructor(public payload: boolean) {}
}

export class GetUserInfo implements Action {
  readonly type = UserInfoActionTypes.GET_USER_INFO;
  constructor() {}
}

export class GetUserInfoSuccess implements Action {
  readonly type = UserInfoActionTypes.GET_USER_INFO_SUCCESS;
  constructor(public payload: User) {}
}

export class GetUserInfoFail implements Action {
  readonly type = UserInfoActionTypes.GET_USER_INFO_FAIL;
  constructor(public payload: { errorMessage: string }) {}
}

export type UserInfoActions =
  | UpdateUserInfo
  | ResetUserInfo
  | UpdateUserAuthorization
  | GetUserInfo
  | GetUserInfoSuccess
  | GetUserInfoFail

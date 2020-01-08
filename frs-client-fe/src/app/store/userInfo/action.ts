import {Action} from '@ngrx/store';

export enum UserInfoActionTypes {
  UPDATE_USERINFO = '[User] Login Success',
  RESET_USERINFO = '[User] Update Token',
  UPDATE_AUTHORIZED = '[User] Toggle Authorized',
  GET_USER_INFO = '[User] Get User Info'
}

export class UpdateUserInfo implements Action {
  readonly type = UserInfoActionTypes.UPDATE_USERINFO;
  constructor(public payload: {
    isAuthenticated?: boolean,
    username?: string
  }) {}
}

export class ResetUserInfo implements Action {
  readonly type = UserInfoActionTypes.RESET_USERINFO;
  constructor() {}
}

export class UpdateUserAuthorization implements Action {
  readonly type = UserInfoActionTypes.UPDATE_AUTHORIZED;
  constructor(public payload: boolean) {}
}

export class GetUserInfo implements Action {
  readonly type = UserInfoActionTypes.GET_USER_INFO;
  constructor(public payload: boolean) {}
}

export type UserInfoActions =
  | UpdateUserInfo
  | ResetUserInfo
  | UpdateUserAuthorization
  | GetUserInfo;

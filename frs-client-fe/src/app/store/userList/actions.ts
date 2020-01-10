import { Action } from '@ngrx/store';

export enum UserListActionTypes {
  FETCH_USERS = '[User List] Fetch Users',
  FETCH_USERS_SUCCESS = '[User List] Fetch Users Success',
  FETCH_USERS_FAIL = '[User List] Fetch Users Fail',
  UPDATE_USER_ROLE = '[User List] Update User Role',
  UPDATE_USER_ROLE_SUCCESS = '[User List] Update User Role Success',
  UPDATE_USER_ROLE_FAIL = '[User List] Update User Role Fail',
  INVITE_USER = '[User List] Invite User'
}

export class FetchUsers implements Action {
  readonly type = UserListActionTypes.FETCH_USERS;
  constructor(public payload: {
    organizationId: string
  }) {}
}

export class FetchUsersSuccess implements Action {
  readonly type = UserListActionTypes.FETCH_USERS_SUCCESS;
  constructor() {}
}

export class FetchUsersFail implements Action {
  readonly type = UserListActionTypes.FETCH_USERS_FAIL;
  constructor(public payload: {
    errorMessage: string
  }) {}
}

export class UpdateUserRole implements Action {
  readonly type = UserListActionTypes.UPDATE_USER_ROLE;
  constructor(public payload: {
    organizationId: string;
    id: string,
    accessLevel: string;
  }) {}
}

export class UpdateUserRoleSuccess implements Action {
  readonly type = UserListActionTypes.UPDATE_USER_ROLE_SUCCESS;
  constructor() {}
}

export class UpdateUserRoleFail implements Action {
  readonly type = UserListActionTypes.UPDATE_USER_ROLE_FAIL;
  constructor(public payload: {
    errorMessage: string;
  }) {}
}

export class InviteUser implements Action {
  readonly type = UserListActionTypes.INVITE_USER;
  constructor(public payload: {
    organizationId: string;
    userEmail: string;
    accessLevel: string;
  }) {}
}

export type UserListActions =
  | FetchUsers
  | FetchUsersSuccess
  | FetchUsersFail
  | UpdateUserRole
  | UpdateUserRoleSuccess
  | UpdateUserRoleFail
  | InviteUser;

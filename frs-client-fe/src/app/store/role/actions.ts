import { Action } from '@ngrx/store';
import { Role } from 'src/app/data/role';

export enum RoleEntityActionList {
  FETCH_ROLES = '[Role/API] Add Roles',
  UPDATE_ROLES = '[Role/API] Update Roles'
};

export class FetchRoles implements Action {
  readonly type = RoleEntityActionList.FETCH_ROLES;
  constructor(public payload: {
    role: Role
  }) {}
}

export class UpdateRoles implements Action {
  readonly type = RoleEntityActionList.UPDATE_ROLES;
  constructor(public payload: {
    role: Role
  }) {}
}

export type RoleEntityActionType =
  | FetchRoles
  | UpdateRoles;

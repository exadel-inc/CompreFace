import { Action } from '@ngrx/store';
import { AppUser } from 'src/app/data/appUser';

export enum UserEntityActionList {
    ADD_USERS = '[User/API] Add Users',
    UPDATE_ROLE = '[User/API] Update role'
};

export class AddUsersEntityAction implements Action {
    readonly type = UserEntityActionList.ADD_USERS;
    constructor(public payload: {
        users: AppUser[]
    }) {}
}

export class UpdateUserRoleEntityAction implements Action {
    readonly type = UserEntityActionList.UPDATE_ROLE;
    constructor(public payload: {
        user: AppUser
    }) {}
}

export type UserEntityActionType =
    | AddUsersEntityAction
    | UpdateUserRoleEntityAction;

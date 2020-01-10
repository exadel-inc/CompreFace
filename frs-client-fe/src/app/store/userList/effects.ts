import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { UserService } from 'src/app/core/user/user.service';
import { Observable, of } from 'rxjs';
import {
  UserListActionTypes,
  FetchUsers,
  FetchUsersSuccess,
  FetchUsersFail,
  UpdateUserRole,
  UpdateUserRoleSuccess,
  UpdateUserRoleFail,
  InviteUser
} from './actions';
import { AddUsersEntityAction, UpdateUserRoleEntityAction } from 'src/app/store/user/action';
import { catchError, switchMap, map } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';

@Injectable()
export class UserListEffect {
  constructor(private actions: Actions, private userService: UserService) { }

  @Effect()
  fetchUserList: Observable<AddUsersEntityAction | FetchUsersSuccess | FetchUsersFail> =
    this.actions.pipe(
      ofType(UserListActionTypes.FETCH_USERS),
      switchMap((action: FetchUsers) => this.userService.getAll(action.payload.organizationId)),
      switchMap((users: AppUser[]) => [new FetchUsersSuccess(), new AddUsersEntityAction({ users: users })]),
      catchError(e => of(new FetchUsersFail({ errorMessage: e })))
    );

  @Effect()
  UpdateUserRole: Observable<UpdateUserRoleEntityAction | UpdateUserRoleSuccess | UpdateUserRoleFail> =
    this.actions.pipe(
      ofType(UserListActionTypes.UPDATE_USER_ROLE),
      switchMap((action: UpdateUserRole) => this.userService.updateRole(
        action.payload.organizationId,
        action.payload.id,
        action.payload.accessLevel
      )),
      switchMap(user => [new UpdateUserRoleSuccess(), new UpdateUserRoleEntityAction({ user })]),
      catchError(e => of(new UpdateUserRoleFail({errorMessage: e})))
    );

  @Effect({
    dispatch: false
  })
  InviteUser: Observable<{message: string}> =
    this.actions.pipe(
      ofType(UserListActionTypes.INVITE_USER),
      switchMap((action: InviteUser) => this.userService.inviteUser(action.payload.organizationId ,action.payload.accessLevel, action.payload.userEmail))
    );
}

import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { UserService } from 'src/app/core/user/user.service';
import { Observable, of, forkJoin } from 'rxjs';
import {
  UserListActionTypes,
  FetchUsers,
  FetchUsersSuccess,
  FetchUsersFail,
  UpdateUserRole,
  UpdateUserRoleSuccess,
  UpdateUserRoleFail,
  InviteUser,
  InviteUserSuccess,
  InviteUserFail
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

  @Effect()
  InviteUser: Observable<InviteUserSuccess | InviteUserFail> =
    this.actions.pipe(
      ofType(UserListActionTypes.INVITE_USER),
      switchMap((action: InviteUser) => forkJoin([
        this.userService.inviteUser(action.payload.organizationId, action.payload.accessLevel, action.payload.userEmail),
        of(action.payload.userEmail)])
      ),
      map((res) => {
        return new InviteUserSuccess({ userEmail: res[1] })
      }),
      catchError(e => of(new InviteUserFail({errorMessage: e})))
    );

    @Effect()
    FetchAvailableRoles: Observable<any> = this.actions
      .pipe(
        ofType(UserListActionTypes.FETCH_AVAILABLE_USER_ROLES),
        
      )
}

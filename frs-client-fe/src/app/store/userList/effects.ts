import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { UserService } from 'src/app/core/user/user.service';
import { of, forkJoin } from 'rxjs';
import {
  FetchUsers,
  FetchUsersSuccess,
  FetchUsersFail,
  UpdateUserRole,
  UpdateUserRoleSuccess,
  UpdateUserRoleFail,
  InviteUser,
  InviteUserSuccess,
  InviteUserFail,
  FetchAllRoles,
  FetchAllRolesFail
} from './actions';
import { AddUsersEntityAction, UpdateUserRoleEntityAction } from 'src/app/store/user/action';
import { catchError, switchMap } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';
import { FetchRolesEntityAction } from 'src/app/store/role/actions';

@Injectable()
export class UserListEffect {
  constructor(private actions: Actions, private userService: UserService) { }

  @Effect()
  fetchUserList =
    this.actions.pipe(
      ofType(FetchUsers),
      switchMap((action) => this.userService.getAll(action.organizationId)),
      switchMap((users: AppUser[]) => [FetchUsersSuccess({}), AddUsersEntityAction({ users: users })]),
      catchError(e => of(FetchUsersFail({ errorMessage: e })))
    );

  @Effect()
  UpdateUserRole =
    this.actions.pipe(
      ofType(UpdateUserRole),
      switchMap((action) => this.userService.updateRole(
        action.organizationId,
        action.id,
        action.accessLevel
      )),
      switchMap(user => [UpdateUserRoleSuccess({}), UpdateUserRoleEntityAction({ user })]),
      catchError(e => of(UpdateUserRoleFail({errorMessage: e})))
    );

  @Effect()
  InviteUser =
    this.actions.pipe(
      ofType(InviteUser),
      switchMap((action) => forkJoin([
        this.userService.inviteUser(action.organizationId, action.accessLevel, action.userEmail),
        of(action)])
      ),
      switchMap((res) => {
        return [InviteUserSuccess({ userEmail: res[1].userEmail }), FetchUsers({organizationId: res[1].organizationId})]
      }),
      catchError(e => of(InviteUserFail({errorMessage: e})))
    );

  @Effect()
  FetchAvailableRoles = this.actions
    .pipe(
      ofType(FetchAllRoles),
      switchMap(action => this.userService.fetchAvailableRoles()),
      switchMap((rolesArray) => [FetchRolesEntityAction({ role: { id:0, accessLevels: rolesArray } })]),
      catchError(e => of(FetchAllRolesFail({ errorMessage: e })))
    )
}

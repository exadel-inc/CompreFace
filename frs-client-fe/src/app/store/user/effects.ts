import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { UserService } from 'src/app/core/user/user.service';
import {
  AddUsersEntityAction,
  UpdateUserRoleEntityAction,
  LoadUsersEntityAction,
  PutUpdatedUserRoleEntityAction
} from 'src/app/store/user/action';
import { switchMap, map } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';
import { FetchRolesEntityAction, LoadRolesEntityAction } from 'src/app/store/role/actions';

@Injectable()
export class UserListEffect {
  constructor(private actions: Actions, private userService: UserService) { }

  @Effect()
  fetchUserList =
    this.actions.pipe(
      ofType(LoadUsersEntityAction),
      switchMap((action) => this.userService.getAll(action.organizationId)),
      map((users: AppUser[]) => AddUsersEntityAction({ users: users }))
    );

  @Effect()
  UpdateUserRole =
    this.actions.pipe(
      ofType(PutUpdatedUserRoleEntityAction),
      switchMap((action) => this.userService.updateRole(
        action.organizationId,
        action.user.id,
        action.user.accessLevel
      )),
      map(user => UpdateUserRoleEntityAction({ user }))
    );

  @Effect()
  FetchAvailableRoles = this.actions
    .pipe(
      ofType(LoadRolesEntityAction),
      switchMap(action => this.userService.fetchAvailableRoles()),
      map((rolesArray) => FetchRolesEntityAction({ role: { id:0, accessLevels: rolesArray } }))
    )
}

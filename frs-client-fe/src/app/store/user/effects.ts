import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {UserService} from 'src/app/core/user/user.service';
import {
  AddUsersEntityAction,
  UpdateUserRoleEntityAction,
  LoadUsersEntityAction,
  PutUpdatedUserRoleEntityAction,
  DeleteUserFromOrganization
} from 'src/app/store/user/action';
import {switchMap, map, catchError} from 'rxjs/operators';
import {AppUser} from 'src/app/data/appUser';
import {FetchRolesEntityAction, LoadRolesEntityAction} from 'src/app/store/role/actions';
import {forkJoin, of} from 'rxjs';
import {OrganizationEnService} from '../organization/organization-entitys.service';

@Injectable()
export class UserListEffect {
  constructor(
    private actions: Actions,
    private userService: UserService,
    private organizationEnService: OrganizationEnService
  ) {}

  @Effect()
  fetchUserList =
    this.actions.pipe(
      ofType(LoadUsersEntityAction),
      switchMap((action) => this.userService.getAll(action.organizationId)),
      map((users: AppUser[]) => AddUsersEntityAction({users}))
    );

  @Effect()
  UpdateUserRole =
    this.actions.pipe(
      ofType(PutUpdatedUserRoleEntityAction),
      switchMap((action) => forkJoin([this.userService.updateRole(
        action.organizationId,
        action.user.id,
        action.user.role
      ), of(action.organizationId)])),
      switchMap(res => {
        const [user, organizationId] = res;
        this.organizationEnService.getAll();

        return [LoadUsersEntityAction({organizationId})];
      })
    );

  @Effect()
  DeleteUserFromOrganization =
      this.actions.pipe(
        ofType(DeleteUserFromOrganization),
        switchMap(action => forkJoin([
          this.userService.delete(action.organizationId, action.userId),
          of(action.organizationId)
        ])),
        map(([deleteResult, organizationId]) => LoadUsersEntityAction({organizationId}))
      );

  @Effect()
  FetchAvailableRoles = this.actions
    .pipe(
      ofType(LoadRolesEntityAction),
      switchMap(() => this.userService.fetchAvailableRoles()),
      // workaround until backend doesnt support available roles call
      catchError(x => of(['OWNER', 'ADMIN', 'USER'])),
      map((rolesArray) => FetchRolesEntityAction({role: {id: 0, accessLevels: rolesArray}}))
    );
}

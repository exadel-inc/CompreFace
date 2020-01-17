import { createAction, props } from '@ngrx/store';

export const FetchUsers = createAction('[User List] Fetch Users', props<{ organizationId: string }>());

export const FetchUsersSuccess = createAction('[User List] Fetch Users Success', props());

export const FetchUsersFail = createAction('[User List] Fetch Users Fail', props<{ errorMessage: string }>());

export const UpdateUserRole = createAction('[User List] Update User Role', props<{
  organizationId: string;
  id: string;
  accessLevel: string;
}>());

export const UpdateUserRoleSuccess = createAction('[User List] Update User Role Success', props());

export const UpdateUserRoleFail = createAction('[User List] Update User Role Fail', props<{ errorMessage: string; }>());

export const InviteUser = createAction('[User List] Invite User', props<{
  organizationId: string;
  userEmail: string;
  accessLevel: string;
}>());

export const InviteUserSuccess = createAction('[User List] Invite User Success', props<{
  userEmail: string;
}>());

export const InviteUserFail = createAction('[User List] Invite User Fail', props<{
  errorMessage: string;
}>());

export const FetchAllRoles = createAction('[User List] Fetch Available User Roles', props());

export const FetchAllRolesFail = createAction('[User List] Fetch Available User Roles Fail', props<{
  errorMessage: string;
}>());

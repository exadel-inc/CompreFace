import {createAction, props} from '@ngrx/store';
import {AppUser} from 'src/app/data/appUser';

export const SetPending = createAction('[User/API] Set Pending', props<{ isPending: boolean }>());
export const LoadUsersEntityAction = createAction('[User/API] Load Users', props<{ organizationId: string }>());
export const AddUsersEntityAction = createAction('[User/API] Add Users', props<{ users: AppUser[] }>());
export const PutUpdatedUserRoleEntityAction = createAction(
  '[User/API] Put Updated User Role',
  props<{ organizationId: string; user: { id: string, role: string } }>()
);
export const UpdateUserRoleEntityAction = createAction('[User/API] Update Role', props<{ user: AppUser }>());
export const DeleteUserFromOrganization = createAction('[User/API] Delete User', props<{ userId: string; organizationId: string }>());

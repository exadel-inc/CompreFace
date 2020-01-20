import { createAction, props } from '@ngrx/store';
import { AppUser } from 'src/app/data/appUser';

export const SetPending = createAction('[User/API] Set Pending', props<{ isPending: boolean }>());

export const AddUsersEntityAction = createAction('[User/API] Add Users', props<{ users: AppUser[] }>());

export const UpdateUserRoleEntityAction = createAction('[User/API] Update role', props<{ user: AppUser }>());

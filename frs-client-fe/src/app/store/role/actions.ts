import { createAction, props } from '@ngrx/store';
import { Role } from 'src/app/data/role';


export const SetPendingRoleEntityAction = createAction('[Role/API] Set Pending', props<{isPending: boolean}>());

export const LoadRolesEntityAction  = createAction('[Role/API] Load Roles');

export const FetchRolesEntityAction = createAction('[Role/API] Add Roles', props<{ role: Role }>());

export const UpdateRolesEntityAction = createAction('[Role/API] Update Roles', props<{id: number, role: Role}>());

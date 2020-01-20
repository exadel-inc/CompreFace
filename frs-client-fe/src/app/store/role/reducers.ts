import { Role } from 'src/app/data/role';
import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { SetPendingRoleEntityAction, FetchRolesEntityAction } from './actions';
import { ActionReducer, createReducer, on } from '@ngrx/store';

export interface RoleEntityState extends EntityState<Role> {
  isPending: boolean;
}

const roleAdapter: EntityAdapter<Role> = createEntityAdapter<Role>();
export const initialState: RoleEntityState = roleAdapter.getInitialState({
  isPending: false
});

export const RoleReducer: ActionReducer<RoleEntityState> = createReducer(
  initialState,
  on(SetPendingRoleEntityAction, (state, { isPending }) => ({
    ...state,
    isPending
  })),
  on(FetchRolesEntityAction, (state, action) => {
    const newState = roleAdapter.removeAll(state);

    return roleAdapter.addOne({ id: 0, accessLevels: action.role.accessLevels }, newState);
  }));

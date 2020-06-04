import {Role} from 'src/app/data/role';
import {createEntityAdapter, EntityAdapter, EntityState} from '@ngrx/entity';
import {SetPendingRoleEntityAction, FetchRolesEntityAction, LoadRolesEntityAction} from './actions';
import {createReducer, on, Action, ActionReducer} from '@ngrx/store';

export interface RoleEntityState extends EntityState<Role> {
  isPending: boolean;
}

const roleAdapter: EntityAdapter<Role> = createEntityAdapter<Role>();
export const initialState: RoleEntityState = roleAdapter.getInitialState({
  isPending: false
});

const reducer: ActionReducer<RoleEntityState> = createReducer(
  initialState,
  on(LoadRolesEntityAction, (state) => ({ ...state, isPending: true })),
  on(SetPendingRoleEntityAction, (state, { isPending }) => ({ ...state, isPending })),
  on(FetchRolesEntityAction, (state, { role }) => {
    const newState = roleAdapter.removeAll(state);
    newState.isPending = false;
    return roleAdapter.addOne({ id: 0, accessLevels: role.accessLevels }, newState);
  })
);

export function RoleReducer(roleState: RoleEntityState, action: Action) {
  return reducer(roleState, action);
}

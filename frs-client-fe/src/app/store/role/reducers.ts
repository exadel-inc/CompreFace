import { Role } from 'src/app/data/role';
import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { RoleEntityActionType, RoleEntityActionList } from './actions';

const roleAdapter: EntityAdapter<Role> = createEntityAdapter<Role>();
export const initialState: EntityState<Role> = roleAdapter.getInitialState();

export function RoleReducer(state = initialState, action: RoleEntityActionType): EntityState<Role> {
  switch(action.type) {
    case RoleEntityActionList.FETCH_ROLES: {
      const newState = roleAdapter.removeAll(state);

      return roleAdapter.addOne({ id: 0, accessLevels: action.payload.role.accessLevels }, newState);
    }

    default:
      return state;
  }
}

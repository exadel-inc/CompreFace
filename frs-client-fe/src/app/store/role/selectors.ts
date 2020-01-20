import { createFeatureSelector, createSelector } from '@ngrx/store';
import { RoleEntityState } from './reducers';

export const selectRoleState = createFeatureSelector<RoleEntityState>('role');
export const selectAllRoles = createSelector(
  selectRoleState,
  (state) => state.ids.length ? state.entities[0].accessLevels : []
);
export const selectIsPending = createSelector(selectRoleState, (state) => state.isPending);

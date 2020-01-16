import { createFeatureSelector, createSelector } from '@ngrx/store';
import { EntityState } from '@ngrx/entity';
import { Role } from 'src/app/data/role';

export const selectRoleState = createFeatureSelector<EntityState<Role>>('role');
export const selectAllRoles = createSelector(
  selectRoleState,
  (state) => state.ids.length ? state.entities[0].accessLevels : []
);

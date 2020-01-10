import { createSelector, createFeatureSelector } from '@ngrx/store';
import { userAdapter } from './reducers';
import { AppUser } from 'src/app/data/appUser';
import { EntityState } from '@ngrx/entity';

export const selectUserEntityState = createFeatureSelector<EntityState<AppUser>>('user');
const { selectEntities, selectIds, selectAll } = userAdapter.getSelectors();

export const selectUserById = (id: string) => createSelector(selectUserEntityState, selectEntities, usersDictionary => usersDictionary[id]);
export const selectUsers = createSelector(selectUserEntityState, selectAll);

import { createSelector, createFeatureSelector } from '@ngrx/store';
import { userAdapter, AppUserEntityState } from './reducers';

export const selectUserEntityState = createFeatureSelector<AppUserEntityState>('user');
const { selectEntities, selectAll } = userAdapter.getSelectors();

export const selectUserById = (id: string) => createSelector(selectUserEntityState, selectEntities, usersDictionary => usersDictionary[id]);
export const selectUsers = createSelector(selectUserEntityState, selectAll);
export const selectIsPending = createSelector(selectUserEntityState, (state) => state.isPending);

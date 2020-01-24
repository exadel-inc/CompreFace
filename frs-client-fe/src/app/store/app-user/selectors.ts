import { createSelector, createFeatureSelector } from '@ngrx/store';
import { AppUserEntityState, appUserAdapter } from './reducers';

const { selectAll } = appUserAdapter.getSelectors();

export const selectAppUserEntityState = createFeatureSelector<AppUserEntityState>('app-user');
export const selectAppUserIsPending = createSelector(selectAppUserEntityState, state => state.isPending);
export const selectAppUsers = createSelector(selectAppUserEntityState, selectAll);

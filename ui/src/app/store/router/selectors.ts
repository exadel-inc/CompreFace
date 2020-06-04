import {createFeatureSelector, createSelector} from '@ngrx/store';
import {RouterStateUrl} from './reducer';
import {RouterReducerState} from '@ngrx/router-store';

export const selectRouterState = createFeatureSelector<RouterReducerState<RouterStateUrl>>('router');

export const SelectRouterIdParam = createSelector(
  selectRouterState,
  ({ state }) => state.params.id || null
  );

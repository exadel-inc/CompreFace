import {createFeatureSelector, createSelector} from "@ngrx/store";
import {AppState} from "../index";
import { ApplicationListState } from './reducers';

export const selectApplicationListState = createFeatureSelector<AppState, ApplicationListState>('applicationList');

export const isLoadingApps = createSelector(
  selectApplicationListState,
  selectApplicationListState => selectApplicationListState.isLoading
);

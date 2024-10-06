import { createFeatureSelector, createSelector } from '@ngrx/store';
import { Plugin } from 'src/app/data/interfaces/plugins';

export const selectPluginState = createFeatureSelector('landmarksPlugin');
export const selectLandmarksPlugin = createSelector(selectPluginState, (state: Plugin) => state);

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { Statistics } from 'src/app/data/interfaces/statistics';

export const selectModelStatisticsState = createFeatureSelector('statistics');
export const selectModelStatistics = createSelector(selectModelStatisticsState, (state: Statistics[]) => state);

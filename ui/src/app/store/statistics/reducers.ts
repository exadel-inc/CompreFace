import { on } from '@ngrx/store';
import { Action, ActionReducer, createReducer } from '@ngrx/store';
import { Statistics } from 'src/app/data/interfaces/statistics';
import { loadModelStatistics, loadModelStatisticsFail, loadModelStatisticsSuccess } from './actions';

const initialState: Statistics[] = [
  {
    requestCount: null,
    createdDate: null,
  },
];

const reducer: ActionReducer<Statistics[]> = createReducer(
  initialState,
  on(loadModelStatistics, () => ({ ...initialState })),
  on(loadModelStatisticsSuccess, (state, action) => ({ ...state, ...action })),
  on(loadModelStatisticsFail, state => ({ ...state }))
);

export const statisticsReducer = (statisticsState: Statistics[], action: Action) => reducer(statisticsState, action);

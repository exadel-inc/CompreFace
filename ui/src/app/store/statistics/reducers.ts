/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
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

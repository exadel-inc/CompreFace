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

import {createSelector, createFeatureSelector} from '@ngrx/store';
import {TestEntityState, testAdapter} from './reducers';

const {selectAll} = testAdapter.getSelectors();

export const selectTestEntityState = createFeatureSelector<TestEntityState>('test');
export const selectTestIsPending = createSelector(selectTestEntityState, state => state.isPending);
export const selectTest = createSelector(selectTestEntityState, selectAll)[0];

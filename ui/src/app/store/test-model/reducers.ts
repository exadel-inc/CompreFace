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

import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Action, ActionReducer, createReducer, on } from '@ngrx/store';
import { AppUser } from 'src/app/data/appUser';

import {
    recognizeFace,
    recognizeFaceSuccess,
    recognizeFaceFail
} from './actions';

export interface TestEntityState extends EntityState<string> {
  isPending: boolean;
}

export const testAdapter = createEntityAdapter<string>();

const initialState: TestEntityState = testAdapter.getInitialState({
  isPending: false
});

const reducer: ActionReducer<TestEntityState> = createReducer(
  initialState,
  on(recognizeFace, (state) => ({ ...state, isPending: true })),
  on(recognizeFaceSuccess, (state,  { model } ) => {
    return model;
  }),
  on(recognizeFaceFail, (state) => ({...state, isPending: false})),
);

export function testReducer(testState: TestEntityState, action: Action) {
  return reducer(testState, action);
}

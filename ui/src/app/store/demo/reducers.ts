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
import { Action, ActionReducer, createReducer, on } from '@ngrx/store';

import { loadDemoApiKeyAction, loadDemoApiKeyFailAction, loadDemoApiKeySuccessAction, setDemoKeyPendingAction } from './actions';

export interface DemoEntityState {
  isPending: boolean;
  apiKey: string;
}

export const initialState: DemoEntityState = {
  isPending: false,
  apiKey: null,
};

const reducer: ActionReducer<DemoEntityState> = createReducer(
  initialState,
  on(loadDemoApiKeyAction, state => ({ ...state, isPending: true })),
  on(loadDemoApiKeySuccessAction, (state, { apiKey }) => ({ ...state, apiKey, isPending: false })),
  on(loadDemoApiKeyFailAction, state => ({ ...state, isPending: false })),
  on(setDemoKeyPendingAction, state => ({ ...state, isPending: true }))
);

export const demoReducer = (demoState: DemoEntityState, action: Action) => reducer(demoState, action);

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
import { ServerStatus } from 'src/app/data/enums/servers-status';
import { getBeServerStatus, getBeServerStatusError, getBeServerStatusSuccess, getCoreServerStatus, getCoreServerStatusError, getCoreServerStatusSuccess, getDbServerStatus, getDbServerStatusError, getDbServerStatusSuccess } from './actions';

export interface ServerStatusInt {
  status: string;
  apiStatus: string;
  coreStatus: string;
}

export const initialState: ServerStatusInt = {
  status: '',
  apiStatus: '',
  coreStatus: '',
};

const reducer: ActionReducer<ServerStatusInt> = createReducer(
  initialState,
  on(getBeServerStatus, getDbServerStatus, getCoreServerStatus, (state, action) => (
    action.preserveState ? { ...state } : { ...initialState }
  )),
  on(getBeServerStatusSuccess, state => ({ ...state, status: ServerStatus.Ready })),
  on(getDbServerStatusSuccess, state => ({ ...state, apiStatus: ServerStatus.Ready })),
  on(getCoreServerStatusSuccess, state => ({ ...state, coreStatus: ServerStatus.Ready })),

  on(getBeServerStatusError, state => ({ ...state, status: '' })),
  on(getDbServerStatusError, state => ({ ...state, apiStatus: '' })),
  on(getCoreServerStatusError, state => ({ ...state, coreStatus: '' }))
);

export const serverStatus = (ServerStatus: ServerStatusInt, action: Action) => reducer(ServerStatus, action);

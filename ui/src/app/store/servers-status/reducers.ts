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
import { getBeServerStatus, getBeServerStatusSuccess } from './actions';

export interface ServerStatusInt {
  status: string;
}

export const initialState: ServerStatusInt = {
  status: '',
};

const reducer: ActionReducer<ServerStatusInt> = createReducer(
  initialState,
  on(getBeServerStatus, () => ({ ...initialState })),
  on(getBeServerStatusSuccess, () => ({ status: ServerStatus.Ready }))
);

export const serverStatus = (ServerStatus: ServerStatusInt, action: Action) => reducer(ServerStatus, action);

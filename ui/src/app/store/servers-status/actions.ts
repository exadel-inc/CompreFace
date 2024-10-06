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

import { createAction, props } from '@ngrx/store';

export const getBeServerStatus = createAction('[Auth] Get Back End Status', props<{ preserveState: boolean }>());
export const getBeServerStatusSuccess = createAction('[Auth] Get Back End Status Success');
export const getBeServerStatusError = createAction('[Auth] Get Back End Status Error');

export const getDbServerStatus = createAction('[Auth] Get DB Status', props<{ preserveState: boolean }>());
export const getDbServerStatusSuccess = createAction('[Auth] Get DB Status Success');
export const getDbServerStatusError = createAction('[Auth] Get DB Status Error');

export const getCoreServerStatus = createAction('[Auth] Get Core Status', props<{ preserveState: boolean }>());
export const getCoreServerStatusSuccess = createAction('[Auth] Get Core Status Success');
export const getCoreServerStatusError = createAction('[Auth] Get Core Status Error');

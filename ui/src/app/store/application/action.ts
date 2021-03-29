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
import { Application } from 'src/app/data/interfaces/application';
import { UserInfo } from '../../data/interfaces/user-info';

export const loadApplications = createAction('[Application] Load Applications');
export const loadApplicationsSuccess = createAction('[Application] Load Applications Success', props<{ applications: Application[] }>());
export const loadApplicationsFail = createAction('[Application] Load Applications Fail', props<{ error: any }>());

export const createApplication = createAction('[Application] Create Application', props<{ name: string }>());
export const createApplicationSuccess = createAction('[Application] Create Application Success', props<{ application: Application }>());
export const createApplicationFail = createAction('[Application] Create Application Fail', props<{ error: any }>());

export const updateApplication = createAction('[Application] Update Application', props<Partial<Application>>());
export const updateApplicationSuccess = createAction('[Application] Update Application Success', props<{ application: Application }>());
export const updateApplicationFail = createAction('[Application] Update Application Fail', props<{ error: any }>());

export const deleteApplication = createAction('[Application] Delete Application', props<Partial<Application>>());
export const deleteApplicationSuccess = createAction('[Application] Delete Application Success', props<{ id: string }>());
export const deleteApplicationFail = createAction('[Application] Delete Application Fail', props<{ error: any }>());

export const setSelectedAppIdEntityAction = createAction('[Application] Set Selected Id Applications', props<{ selectedAppId }>());

export const refreshApplication = createAction('[Application] Refresh Application', props<UserInfo>());

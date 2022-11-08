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
import { MailServiceStatus } from 'src/app/data/interfaces/mail-service-status';
import { getMailServiceStatusSuccess, getMailServiceStatus, getMailServiceStatusFail } from './actions';

const initialState: MailServiceStatus = {
  mailServiceEnabled: false,
};

const reducer: ActionReducer<MailServiceStatus> = createReducer(
  initialState,
  on(getMailServiceStatus, () => ({ ...initialState })),
  on(getMailServiceStatusSuccess, (state, action) => ({ ...state, ...action })),
  on(getMailServiceStatusFail, state => ({ ...state }))
);

export const mailServiceStatusReducer = (serviceStatus: MailServiceStatus, action: Action) => reducer(serviceStatus, action);

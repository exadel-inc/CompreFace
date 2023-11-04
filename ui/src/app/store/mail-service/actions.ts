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
import { MailServiceStatus } from 'src/app/data/interfaces/mail-service-status';

export const getMailServiceStatus = createAction('[Get Mailing Service Status] App');
export const getMailServiceStatusSuccess = createAction('[Get Mailing Service Status Success] App', props<MailServiceStatus>());
export const getMailServiceStatusFail = createAction('[Get Mailing Service Status Fail] App', props<{ error: any }>());

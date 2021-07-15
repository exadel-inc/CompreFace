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

export const loadSubjects = createAction('[Collection] Load Subjects', props<{ apiKey: string }>());
export const loadSubjectsSuccess = createAction('[Collection] Load Subjects Success', props<{ subjects: string[] }>());
export const loadSubjectsFail = createAction('[Collection] Load Subjects Fail', props<{ error: any }>());

export const addSubject = createAction('[Collection] Add Subjects', props<{ name: string; apiKey: string }>());
export const addSubjectSuccess = createAction('[Collection] Add Subjects Success', props<{ subject: string }>());
export const addSubjectFail = createAction('[Collection] Add Subjects Fail', props<{ error: any }>());

export const resetSubjects = createAction('[Collection] Reset Subjects');

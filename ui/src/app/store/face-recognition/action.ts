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
import { Model } from 'src/app/data/interfaces/model';

export const recognizeFace = createAction('[Model] Face Recognize', props<{ file: any }>());
export const recognizeFaceSuccess = createAction('[Model] Face Recognize Success', props<{ model: any; file: any; request: any }>());
export const recognizeFaceFail = createAction('[Model] Face Recognize Fail', props<{ error: any }>());
export const recognizeFaceReset = createAction('[Model] Face Recognize Reset');

export const addFace = createAction('[Model] Add Face', props<{ file: any; model: Model }>());
export const addFaceSuccess = createAction('[Model] Add Face Success', props<{ model: any }>());
export const addFaceFail = createAction('[Model] Add Face Fail', props<{ error: any }>());

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

export const verifyFace = createAction('[Model] Face Verification Save');
export const verifyFaceAddProcessFile = createAction('[Model] Face Verification Add Process File', props<{ processFile: any }>());
export const verifyFaceAddCheckFileFile = createAction('[Model] Face Verification Add Check File', props<{ checkFile: any }>());
export const verifyFaceSuccess = createAction('[Model] Face Verification Success', props<{ model: any; request: any }>());
export const verifyFaceFail = createAction('[Model] Face Verification Fail', props<{ error: any }>());
export const verifyFaceReset = createAction('[Model] Face Verification Reset');
export const verifyFaceProcessFileReset = createAction('[Model] Face Verification Reset Process File');
export const verifyFaceCheckFileReset = createAction('[Model] Face Verification Reset Check File');

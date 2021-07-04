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

import {
  verifyFaceFail,
  verifyFaceReset,
  verifyFace,
  verifyFaceSuccess,
  verifyFaceProcessFileReset,
  verifyFaceCheckFileReset,
  verifyFaceAddProcessFile,
  verifyFaceAddCheckFileFile,
} from './action';
export interface FaceVerificationEntityState {
  isPending: boolean;
  model: any;
  processFile?: any;
  checkFile?: any;
  request: any;
}

const initialStateVerification: FaceVerificationEntityState = {
  isPending: false,
  model: null,
  processFile: null,
  checkFile: null,
  request: null,
};

const reducerVerification: ActionReducer<FaceVerificationEntityState> = createReducer(
  initialStateVerification,
  on(verifyFaceAddProcessFile, verifyFaceAddCheckFileFile, (state, action) => ({ ...state, ...action })),
  on(verifyFace, state => ({ ...state, isPending: true })),
  on(verifyFaceSuccess, (state, action) => ({ ...state, ...action, isPending: false })),
  on(verifyFaceFail, state => ({ ...state, isPending: false })),
  on(verifyFaceProcessFileReset, state => ({ ...state, processFile: null, request: null, model: null })),
  on(verifyFaceCheckFileReset, state => ({ ...state, checkFile: null, request: null, model: null })),
  on(verifyFaceReset, () => ({ ...initialStateVerification }))
);

export const faceVerificationReducer = (verificationState: FaceVerificationEntityState, action: Action) =>
  reducerVerification(verificationState, action);

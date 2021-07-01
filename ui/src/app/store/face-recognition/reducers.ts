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
import { recognizeFace, recognizeFaceFail, recognizeFaceReset, recognizeFaceSuccess } from './action';

export interface FaceRecognitionEntityState {
  isPending: boolean;
  model: any;
  file: any;
  request: any;
}

const initialState: FaceRecognitionEntityState = {
  isPending: false,
  model: null,
  file: null,
  request: null,
};

const reducer: ActionReducer<FaceRecognitionEntityState> = createReducer(
  initialState,
  on(recognizeFace, (state, action) => ({ ...state, ...action, isPending: true })),
  on(recognizeFaceSuccess, (state, action) => ({ ...state, ...action, isPending: false })),
  on(recognizeFaceFail, state => ({ ...state, isPending: false })),
  on(recognizeFaceReset, () => ({ ...initialState }))
);

export const faceRecognitionReducer = (recognitionState: FaceRecognitionEntityState, action: Action) => reducer(recognitionState, action);

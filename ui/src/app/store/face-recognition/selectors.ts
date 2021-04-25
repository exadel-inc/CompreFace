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
import { createFeatureSelector, createSelector } from '@ngrx/store';

import { FaceRecognitionEntityState } from './reducers';

export const selectTestEntityState = createFeatureSelector<FaceRecognitionEntityState>('faceRecognition');
export const selectTestIsPending = createSelector(selectTestEntityState, state => state.isPending);

export const selectFaceData = createSelector(selectTestEntityState, state => (state.model ? state.model.result : null));
export const selectFile = createSelector(selectTestEntityState, state => state.file);
export const selectStateReady = createSelector(selectTestEntityState, state => !state.isPending && !!state?.model?.result[0]);
export const selectRequest = createSelector(selectTestEntityState, state => ({
  request: state.request,
  response: JSON.stringify(state.model, undefined, 2),
}));

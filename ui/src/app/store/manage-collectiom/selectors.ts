/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { CollectionEntityState } from './reducers';

export const selectCollectionEntityState = createFeatureSelector<CollectionEntityState>('manage-collection');
export const selectCollectionSubjects = createSelector(selectCollectionEntityState, (state: CollectionEntityState) => state.subjects);
export const selectCollectionSubject = createSelector(selectCollectionEntityState, (state: CollectionEntityState) => state.subject);
export const selectAddSubjectPending = createSelector(selectCollectionEntityState, (state: CollectionEntityState) => state.isPending);
export const selectImageCollection = createSelector(selectCollectionEntityState, (state: CollectionEntityState) => state.collection);
export const selectImageCollectionPending = createSelector(selectCollectionEntityState, (state: CollectionEntityState) => state.isCollectionPending);
export const selectSubjectMode = createSelector(selectCollectionEntityState, (state: CollectionEntityState) => state.mode);
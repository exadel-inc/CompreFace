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
import { CollectionHelper } from 'src/app/core/collection/collection.helper';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';
import { CollectionItem } from 'src/app/data/interfaces/collection';

import {
  addFileToCollection,
  addSubject,
  addSubjectFail,
  addSubjectSuccess,
  deleteSubject,
  deleteSubjectFail,
  deleteSubjectSuccess,
  editSubject,
  editSubjectFail,
  editSubjectSuccess,
  loadSubjects,
  loadSubjectsFail,
  loadSubjectsSuccess,
  resetSubjects,
  setSelectedSubject,
  uploadImage,
  uploadImageSuccess,
  uploadImageFail,
  getSubjectExamplesSuccess,
  deleteSubjectExample,
  deleteSubjectExampleFail,
  deleteSubjectExampleSuccess,
  deleteItemFromUploadOrder,
  getSubjectExamples,
  getSubjectExamplesFail,
  resetSubjectExamples,
} from './action';

function updateCollectionItemStatus(
  state: CollectionEntityState,
  item: CollectionItem,
  status: CircleLoadingProgressEnum,
  error?: string
): CollectionEntityState {
  const collectionCopy = [...state.collection];
  const targetItemIndex = collectionCopy.findIndex(collectionCopyItem => item.url === collectionCopyItem.url);

  if (~targetItemIndex) {
    collectionCopy[targetItemIndex] = {
      ...collectionCopy[targetItemIndex],
      status,
    };

    if (error) {
      collectionCopy[targetItemIndex].error = error;
    }
  }

  return {
    ...state,
    collection: collectionCopy,
  };
}

export interface CollectionEntityState {
  isPending: boolean;
  isCollectionPending: boolean;
  subjects: string[];
  subject: string;
  collection: CollectionItem[];
}

const initialState: CollectionEntityState = {
  isPending: false,
  isCollectionPending: false,
  subjects: null,
  subject: null,
  collection: [],
};

const reducer: ActionReducer<CollectionEntityState> = createReducer(
  initialState,
  on(loadSubjects, addSubject, editSubject, deleteSubject, state => ({ ...state, isPending: true })),
  on(addSubjectSuccess, addSubjectSuccess, editSubjectSuccess, (state, { subject }) => ({
    ...state,
    isPending: false,
    subject,
  })),
  on(deleteSubjectSuccess, state => ({ ...state, isPending: false, subject: null })),
  on(loadSubjectsFail, addSubjectFail, editSubjectFail, deleteSubjectFail, deleteSubjectSuccess, state => ({ ...state, isPending: false })),
  on(loadSubjectsSuccess, (state, { subjects }) => ({ ...state, isPending: false, subjects })),
  on(setSelectedSubject, (state, { subject }) => ({ ...state, subject })),
  on(resetSubjects, () => ({ ...initialState })),
  on(getSubjectExamples, state => ({ ...state, isCollectionPending: true })),
  on(getSubjectExamplesSuccess, (state, { items, apiKey }) => {
    const collectionCopy = [...state.collection.filter(item => item.status !== CircleLoadingProgressEnum.Uploaded)];

    const newCollectionItems = items.map(item => ({
      url: CollectionHelper.getCollectionItemUrl(apiKey, item.image_id),
      id: item.image_id,
      status: CircleLoadingProgressEnum.Uploaded,
      subject: item.subject,
    }));

    return {
      ...state,
      isCollectionPending: false,
      collection: [...newCollectionItems, ...collectionCopy],
    };
  }),
  on(getSubjectExamplesFail, state => ({ ...state, isCollectionPending: false })),
  on(addFileToCollection, (state, { url, file, subject }) => ({
    ...state,
    collection: [...state.collection, { url, file, subject, status: CircleLoadingProgressEnum.OnHold }],
  })),
  on(uploadImage, deleteSubjectExample, (state, { item }) => updateCollectionItemStatus(state, item, CircleLoadingProgressEnum.InProgress)),
  on(uploadImageSuccess, deleteSubjectExampleSuccess, (state, { item }) =>
    updateCollectionItemStatus(state, item, CircleLoadingProgressEnum.Uploaded)
  ),
  on(uploadImageFail, deleteSubjectExampleFail, (state, { item, error }) =>
    updateCollectionItemStatus(state, item, CircleLoadingProgressEnum.Failed, error)
  ),
  on(deleteItemFromUploadOrder, (state, { item }) => {
    const collection = state.collection.filter(collectionItem => collectionItem.url !== item.url);

    return {
      ...state,
      collection,
    };
  }),
  on(resetSubjectExamples, state => ({ ...state, collection: [] }))
);

export const collectionReducer = (modelState: CollectionEntityState, action: Action) => reducer(modelState, action);

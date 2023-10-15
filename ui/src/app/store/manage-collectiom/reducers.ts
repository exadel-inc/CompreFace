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
import { SubjectModeEnum } from 'src/app/data/enums/subject-mode.enum';
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
  deleteSubjectExample,
  deleteSubjectExampleFail,
  deleteSubjectExampleSuccess,
  deleteItemFromUploadOrder,
  getSubjectExamples,
  getSubjectExamplesSuccess,
  getSubjectExamplesFail,
  resetSubjectExamples,
  setSubjectMode,
  toggleExampleSelection,
  resetSelectedExamples,
  deleteSelectedExamples,
  deleteSelectedExamplesFail,
  deleteSelectedExamplesSuccess,
  getSubjectMediaNextPage,
  getNextPageSubjectExamplesSuccess,
} from './action';

function updateCollectionItemStatus(
  state: CollectionEntityState,
  item: CollectionItem,
  status: CircleLoadingProgressEnum,
  error?: string,
  id?: string
): CollectionEntityState {
  const collectionCopy = [...state.collection];
  const targetItemIndex = collectionCopy.findIndex(collectionCopyItem => item.url === collectionCopyItem.url);

  if (~targetItemIndex) {
    collectionCopy[targetItemIndex] = {
      ...collectionCopy[targetItemIndex],
      status,
      id,
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
  mode: SubjectModeEnum;
}

const initialState: CollectionEntityState = {
  isPending: false,
  isCollectionPending: false,
  subjects: null,
  subject: null,
  collection: [],
  mode: SubjectModeEnum.Default,
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
  on(setSelectedSubject, (state, { subject }) => ({ ...state, subject, collection: [] })),
  on(resetSubjects, () => ({ ...initialState })),
  on(getSubjectExamples, state => ({ ...state, isCollectionPending: true })),
  on(getSubjectExamplesSuccess, (state, { items, apiKey }) => {
    const collectionCopy = [
      ...state.collection.filter(item => item.status !== CircleLoadingProgressEnum.Uploaded && item.subject === state.subject),
    ];

    const newCollectionItems = items.map(item => ({
      url: CollectionHelper.getCollectionItemUrl(apiKey, item.image_id),
      id: item.image_id,
      status: CircleLoadingProgressEnum.Uploaded,
      subject: item.subject,
      page: item['page'],
      totalPages: item['totalPages'],
      totalElements: item['totalElements'],
    }));

    return {
      ...state,
      isCollectionPending: false,
      collection: [...newCollectionItems, ...collectionCopy],
    };
  }),

  on(getSubjectMediaNextPage, state => ({ ...state, isCollectionPending: false })),
  on(getNextPageSubjectExamplesSuccess, (state, { items, apiKey }) => {
    const collectionCopy = [
      ...state.collection.filter(
        item =>
          item.status !== CircleLoadingProgressEnum.Uploaded &&
          item.status !== CircleLoadingProgressEnum.Failed &&
          item.subject === state.subject
      ),
    ];

    let prevCollection = [...state.collection];

    const newCollectionItems = items.map(item => ({
      url: CollectionHelper.getCollectionItemUrl(apiKey, item.image_id),
      id: item.image_id,
      status: CircleLoadingProgressEnum.Uploaded,
      subject: item.subject,
      page: item['page'],
      totalPages: item['totalPages'],
      totalElements: item['totalElements'],
    }));

    return {
      ...state,
      isCollectionPending: false,
      collection: [...prevCollection, ...newCollectionItems, ...collectionCopy],
    };
  }),

  on(getSubjectExamplesFail, state => ({ ...state, isCollectionPending: false })),

  on(addFileToCollection, (state, { url, file, subject }) => {
    return {
      ...state,
      collection: [
        ...state.collection.filter(item => item.subject === state.subject && item.url !== url),
        { url, file, subject, status: CircleLoadingProgressEnum.OnHold },
      ],
    };
  }),
  on(uploadImage, deleteSubjectExample, (state, { item }) => updateCollectionItemStatus(state, item, CircleLoadingProgressEnum.InProgress)),
  on(uploadImageSuccess, (state, { item, itemId }) =>
    updateCollectionItemStatus(state, item, CircleLoadingProgressEnum.Uploaded, null, itemId)
  ),
  on(deleteSubjectExampleSuccess, (state, { item }) => updateCollectionItemStatus(state, item, CircleLoadingProgressEnum.Uploaded)),
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
  on(resetSubjectExamples, state => ({ ...state, collection: [] })),
  on(setSubjectMode, (state, { mode }) => ({ ...state, mode })),
  on(toggleExampleSelection, (state, { item }) => {
    const collectionCopy = [...state.collection];
    const targetItemIndex = collectionCopy.indexOf(item);

    if (~targetItemIndex) {
      collectionCopy[targetItemIndex] = {
        ...item,
        isSelected: !item.isSelected,
      };
    }

    return {
      ...state,
      collection: collectionCopy,
    };
  }),
  on(resetSelectedExamples, state => {
    return {
      ...state,
      collection: state.collection.map(item => (item.isSelected ? { ...item, isSelected: false } : item)),
    };
  }),
  on(deleteSelectedExamples, state => ({ ...state, isCollectionPending: true })),
  on(deleteSelectedExamplesSuccess, state => ({ ...state, isCollectionPending: false })),
  on(deleteSelectedExamplesFail, state => ({ ...state, isCollectionPending: false }))
);

export const collectionReducer = (modelState: CollectionEntityState, action: Action) => reducer(modelState, action);

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
import { SubjectModeEnum } from 'src/app/data/enums/subject-mode.enum';
import { CollectionItem, SubjectExampleResponseItem } from 'src/app/data/interfaces/collection';

export const loadSubjects = createAction('[Collection] Load Subjects', props<{ apiKey: string }>());
export const loadSubjectsSuccess = createAction('[Collection] Load Subjects Success', props<{ subjects: string[] }>());
export const loadSubjectsFail = createAction('[Collection] Load Subjects Fail', props<{ error: any }>());

export const addSubject = createAction('[Collection] Add Subjects', props<{ name: string; apiKey: string }>());
export const addSubjectSuccess = createAction('[Collection] Add Subjects Success', props<{ subject: string }>());
export const addSubjectFail = createAction('[Collection] Add Subjects Fail', props<{ error: any }>());

export const editSubject = createAction('[Collection] Edit Subject', props<{ editName: string; apiKey: string; subject: string }>());
export const editSubjectSuccess = createAction('[Collection] Edit Subject Success', props<{ subject: string }>());
export const editSubjectFail = createAction('[Collection] Edit Subject Fail', props<{ error: any }>());

export const deleteSubject = createAction('[Collection] Delete Subject', props<{ name: string; apiKey: string }>());
export const deleteSubjectSuccess = createAction('[Collection] Delete Subject Success');
export const deleteSubjectFail = createAction('[Collection] Delete Subject Fail', props<{ error: any }>());

export const resetSubjects = createAction('[Collection] Reset Subjects');
export const setSelectedSubject = createAction('[Collection] Set Selected Subject', props<{ subject: any }>());
export const initSelectedSubject = createAction('[Collection] Init Selected Subject');

export const getSubjectExamples = createAction('[Collection] Get Subject Examples Request', props<{ subject: string }>());
export const getSubjectExamplesSuccess = createAction(
  '[Collection] Get Subject Examples Success',
  props<{ items: SubjectExampleResponseItem[]; apiKey: string }>()
);
export const getSubjectExamplesFail = createAction('[Collection] Get Subject Examples Fail', props<{ error: string }>());

export const getSubjectMediaNextPage = createAction(
  '[Collection] Get Next Page Request',
  props<{ subject: string; page: number; totalPages: number }>()
);
export const getNextPageSubjectExamplesSuccess = createAction(
  '[Collection] Get Next Page Subject Examples Success',
  props<{ items: SubjectExampleResponseItem[]; apiKey: string }>()
);

export const readImageFiles = createAction('[Collection] Read Image Files', props<{ fileDescriptors: File[] }>());
export const addFileToCollection = createAction(
  '[Collection] Add File to Collection',
  props<{ url: string; file: File; subject: string }>()
);

export const uploadImage = createAction(
  '[Collection] Upload File to Collection Request',
  props<{ item: CollectionItem; continueUpload?: boolean }>()
);
export const uploadImageSuccess = createAction(
  '[Collection] Upload File to Collection Success',
  props<{ item: CollectionItem; itemId: string; continueUpload?: boolean }>()
);
export const uploadImageFail = createAction(
  '[Collection] Upload File to Collection Fail',
  props<{ error: any; item: CollectionItem; continueUpload?: boolean }>()
);

export const deleteSubjectExample = createAction('[Collection] Delete Subject Example Request', props<{ item: CollectionItem }>());
export const deleteSubjectExampleSuccess = createAction('[Collection] Delete Subject Example Success', props<{ item: CollectionItem }>());
export const deleteSubjectExampleFail = createAction(
  '[Collection] Delete Subject Example Fail',
  props<{ error: any; item: CollectionItem }>()
);

export const startUploadImageOrder = createAction('[Collection] Start Upload Image Order');
export const finishUploadImageOrder = createAction('[Collection] Finish Upload Image Order');
export const deleteItemFromUploadOrder = createAction('[Collection] Delete Item From Upload Order', props<{ item: CollectionItem }>());

export const resetSubjectExamples = createAction('[Collection] Reset Subject Examples');

export const setSubjectMode = createAction('[Collection] Set Subject Mode', props<{ mode: SubjectModeEnum }>());

export const toggleExampleSelection = createAction('[Collection] Toggle Example Selected State', props<{ item: CollectionItem }>());
export const resetSelectedExamples = createAction('[Collection] Reset Selected Examples');
export const deleteSelectedExamples = createAction('[Collection] Delete Selected Examples', props<{ ids: string[] }>());
export const deleteSelectedExamplesSuccess = createAction('[Collection] Delete Selected Examples Success');
export const deleteSelectedExamplesFail = createAction('[Collection] Delete Selected Examples Fail', props<{ error: any }>());

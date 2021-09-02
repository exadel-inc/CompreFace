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
import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { selectCurrentApiKey } from '../../store/model/selectors';
import { selectAddSubjectPending, selectCollectionSubject, selectCollectionSubjects, selectImageCollection, selectImageCollectionPending } from '../../store/manage-collectiom/selectors';
import { deleteItemFromUploadOrder, deleteSubject, deleteSubjectExample, editSubject, getSubjectExamples, readImageFiles, resetSubjectExamples, uploadImage } from '../../store/manage-collectiom/action';
import { CollectionItem } from 'src/app/data/interfaces/collection';

@Injectable()
export class CollectionRightFacade {
  subjects$: Observable<string[]>;
  subject$: Observable<string>;
  apiKey$: Observable<string>;
  collectionItems$: Observable<CollectionItem[]>;
  isPending$: Observable<boolean>;
  isCollectionPending$: Observable<boolean>;

  constructor(private store: Store<any>) {
    this.subjects$ = this.store.select(selectCollectionSubjects);
    this.subject$ = this.store.select(selectCollectionSubject);
    this.apiKey$ = this.store.select(selectCurrentApiKey);
	this.collectionItems$ = this.store.select(selectImageCollection);
    this.isPending$ = this.store.select(selectAddSubjectPending);
	this.isCollectionPending$ = this.store.select(selectImageCollectionPending);
  }


  edit(editName: string, subject: string,  apiKey: string): void {
    this.store.dispatch(editSubject({ editName, apiKey, subject }));
  }

  delete(name: string, apiKey: string): void {
    this.store.dispatch(deleteSubject({ name, apiKey }));
  }

  loadExamplesList(): void {
	  this.store.dispatch(getSubjectExamples())
  }

  addImageFilesToCollection(fileDescriptors: File[]): void {
	this.store.dispatch(readImageFiles({fileDescriptors}));
  }

  uploadImage(item: CollectionItem): void {
	this.store.dispatch(uploadImage({item}));
  }

  deleteSubjectExample(item: CollectionItem): void {
	  this.store.dispatch(deleteSubjectExample({item}));
  }

  deleteItemFromUploadOrder(item: CollectionItem): void {
	  this.store.dispatch(deleteItemFromUploadOrder({item}));
  }

  resetSubjectExamples(): void {
	  this.store.dispatch(resetSubjectExamples());
  }
}

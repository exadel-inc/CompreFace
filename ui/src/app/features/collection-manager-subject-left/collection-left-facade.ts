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

import { addSubject, deleteSubject, editSubject, loadSubjects, setSelectedSubject } from '../../store/manage-collectiom/action';
import { selectAddSubjectPending, selectCollectionSubject, selectCollectionSubjects } from '../../store/manage-collectiom/selectors';
import { selectCurrentApiKey } from '../../store/model/selectors';

@Injectable()
export class CollectionLeftFacade {
  subjectsList$: Observable<string[]>;
  currentSubject$: Observable<string>;
  apiKey$: Observable<string>;
  isPending$: Observable<boolean>;

  constructor(private store: Store<any>) {
    this.subjectsList$ = this.store.select(selectCollectionSubjects);
    this.currentSubject$ = this.store.select(selectCollectionSubject);
    this.isPending$ = this.store.select(selectAddSubjectPending);
    this.apiKey$ = this.store.select(selectCurrentApiKey);
  }

  edit(editName: string, subject: string, apiKey: string): void {
    this.store.dispatch(editSubject({ editName, apiKey, subject }));
  }

  delete(name: string, apiKey: string): void {
    this.store.dispatch(deleteSubject({ name, apiKey }));
  }

  loadSubjects(apiKey: string): void {
    this.store.dispatch(loadSubjects({ apiKey }));
  }

  addSubject(name: string, apiKey: string): void {
    this.store.dispatch(addSubject({ name, apiKey }));
  }

  onSelectedSubject(subject: string): void {
    this.store.dispatch(setSelectedSubject({ subject }));
  }
}

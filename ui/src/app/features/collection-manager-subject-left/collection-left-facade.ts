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
import { filter, finalize, map, take, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { addSubject, loadSubjects } from '../../store/manage-collectiom/action';
import { selectCurrentModel } from '../../store/model/selectors';
import { selectAddSubjectPending, selectCollectionSubjects } from '../../store/manage-collectiom/selectors';

@Injectable()
export class CollectionLeftFacade {
  subjectsList$: Observable<string[]>;
  isPending$: Observable<boolean>;

  private apiKey: string;

  constructor(private store: Store<any>) {
    this.subjectsList$ = this.store.select(selectCollectionSubjects);
    this.isPending$ = this.store.select(selectAddSubjectPending);
  }

  initUrlBindingStreams(): void {
    this.store
      .select(selectCurrentModel)
      .pipe(
        take(2),
        filter(model => !!model),
        map(({ apiKey }) => apiKey),
        tap(apiKey => (this.apiKey = apiKey))
      )
      .subscribe();
  }

  loadSubjects(): void {}

  addSubject(name: string): void {
    this.store.dispatch(addSubject({ name, apiKey: this.apiKey }));
  }
}

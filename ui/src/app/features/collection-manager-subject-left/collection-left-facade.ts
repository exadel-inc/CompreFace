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
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { filter, takeUntil, tap } from 'rxjs/operators';
import { Subject } from 'rxjs';

import { addSubject, addSubjectSuccess } from '../../store/manage-collectiom/action';
import { CollectionEntityState } from '../../store/manage-collectiom/reducers';
import { selectCollectionApiKey } from '../../store/manage-collectiom/selectors';

@Injectable()
export class CollectionLeftFacade {
  private apiKey: string;
  private unsubscribe$: Subject<void> = new Subject();

  constructor(private route: ActivatedRoute, private store: Store<CollectionEntityState>) {}

  initUrlBindingStreams() {
    this.store
      .select(selectCollectionApiKey)
      .pipe(
        takeUntil(this.unsubscribe$),
        filter(apiKey => !!apiKey),
        tap(apiKey => (this.apiKey = apiKey))
      )
      .subscribe();
  }

  selectedSubject(subject: string): void {
    this.store.dispatch(addSubjectSuccess({ subject }));
  }

  addSubject(name: string): void {
    this.store.dispatch(addSubject({ name, apiKey: this.apiKey }));
  }

  unsubscribe() {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}

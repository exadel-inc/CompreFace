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
import { Subject } from 'rxjs';
import { Store } from '@ngrx/store';
import { CollectionEntityState } from '../../store/manage-collectiom/reducers';
import { selectCollectionApiKey } from '../../store/manage-collectiom/selectors';
import { filter, takeUntil, tap } from 'rxjs/operators';
import { deleteSubject, editSubject } from '../../store/manage-collectiom/action';

@Injectable()
export class CollectionRightFacade {
  private apiKey: string;
  private unsubscribe$: Subject<void> = new Subject();

  constructor(private store: Store<CollectionEntityState>) {}

  initUrlBindingStreams(): void {
    this.store
      .select(selectCollectionApiKey)
      .pipe(
        takeUntil(this.unsubscribe$),
        filter(apiKey => !!apiKey),
        tap(apiKey => (this.apiKey = apiKey))
      )
      .subscribe();
  }

  editSubject(editName: string, subject: string): void {
    this.store.dispatch(editSubject({ name: editName, apiKey: this.apiKey, subject }));
  }

  deleteSubject(subject: string): void {
    this.store.dispatch(deleteSubject({ apiKey: this.apiKey, subject }));
  }

  unsubscribe(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}

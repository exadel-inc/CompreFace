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
import { addSubject } from '../../store/manage-collectiom/action';
import {filter, take, tap} from "rxjs/operators";
import {selectCollectionApiKey} from "../../store/manage-collectiom/selectors";

@Injectable()
export class CollectionLeftFacade {
  private apiKey: string;

  constructor(private route: ActivatedRoute, private store: Store<any>) {}

  initUrlBindingStreams() {
    this.store.select(selectCollectionApiKey).pipe(
            take(2),filter(apiKey => !!apiKey),
            tap(apiKey => (this.apiKey = apiKey)),
        )
        .subscribe();
  }

  addSubject(name: string) {
    this.store.dispatch(addSubject({ name, apiKey: this.apiKey }));
  }
}

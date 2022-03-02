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
import { Resolve } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';

import { AppState } from '../../store';
import { selectUserId } from '../../store/userInfo/selectors';
import { getUserInfo } from '../../store/userInfo/action';

@Injectable({ providedIn: 'root' })
export class UserInfoResolver implements Resolve<boolean> {
  loggedUserId$: Observable<string>;

  constructor(private store: Store<AppState>) {
    this.loggedUserId$ = this.store.select(selectUserId);
  }

  resolve(): Promise<boolean> {
    return new Promise<boolean>(resolve => {
      this.loggedUserId$.pipe(take(1)).subscribe(userId => {
        if (!userId) {
          this.store.dispatch(getUserInfo());
        }
        return resolve(true);
      });
    });
  }
}

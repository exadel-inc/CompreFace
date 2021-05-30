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
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs';
import { filter, take } from 'rxjs/operators';

import { Routes } from '../../data/enums/routers-url.enum';
import { AppState } from '../../store';
import { loadApplications, setSelectedAppIdEntityAction } from '../../store/application/action';
import { selectApplications } from '../../store/application/selectors';
import { getUserInfo } from '../../store/userInfo/action';

@Injectable()
export class ApplicationPageService {
  private appsSub: Subscription;
  private appId: string;

  constructor(private router: Router, private route: ActivatedRoute, private store: Store<AppState>) {}

  initUrlBindingStreams() {
    this.appId = this.route.snapshot.queryParams.app;

    if (this.appId) {
      this.store.dispatch(setSelectedAppIdEntityAction({ selectedAppId: this.appId }));
      this.appsSub = this.store
        .select(selectApplications)
        .pipe(
          filter(apps => !apps.length),
          take(1)
        )
        .subscribe(() => {
          this.fetchApps();
        });
    } else {
      this.router.navigate([Routes.Home]);
    }
  }

  unSubscribe() {
    if (this.appsSub) {
      this.appsSub.unsubscribe();
    }
  }

  fetchApps() {
    this.store.dispatch(loadApplications());
    this.store.dispatch(getUserInfo());
  }
}

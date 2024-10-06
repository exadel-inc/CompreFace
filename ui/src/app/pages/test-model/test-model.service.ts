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
import { loadModels, setSelectedModelIdEntityAction } from '../../store/model/action';
import { selectModels } from '../../store/model/selectors';
import { getUserInfo } from '../../store/userInfo/action';
import { ServiceTypes } from '../../data/enums/service-types.enum';

@Injectable()
export class TestModelPageService {
  private modelSub: Subscription;
  private appId: string;
  private modelId: string;
  private type: ServiceTypes;

  constructor(private router: Router, private route: ActivatedRoute, private store: Store<AppState>) {}

  initUrlBindingStreams() {
    this.appId = this.route.snapshot.queryParams.app;
    this.modelId = this.route.snapshot.queryParams.model;
    this.type = this.route.snapshot.queryParams.type;

    if (this.appId && this.modelId) {
      this.store.dispatch(setSelectedAppIdEntityAction({ selectedAppId: this.appId }));
      this.store.dispatch(setSelectedModelIdEntityAction({ selectedModelId: this.modelId }));
      this.store.dispatch(loadApplications());
      this.store.dispatch(getUserInfo());
      this.modelSub = this.store
        .select(selectModels)
        .pipe(
          filter(models => !models.length),
          take(1)
        )
        .subscribe(() => {
          this.fetchModels();
        });
    } else {
      this.router.navigate([Routes.Home]);
    }
  }

  getServiceType() {
    return this.type;
  }

  fetchModels() {
    this.store.dispatch(loadModels({ applicationId: this.appId }));
  }

  clearSelectedModelId() {
    this.store.dispatch(setSelectedModelIdEntityAction({ selectedModelId: null }));
  }

  unSubscribe() {
    this.modelSub.unsubscribe();
  }
}

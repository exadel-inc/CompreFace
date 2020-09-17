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
import { AppState } from '../../store';
import { loadApplications, setSelectedAppIdEntityAction } from '../../store/application/action';
import { ROUTERS_URL } from '../../data/routers-url.variable';
import { setSelectedId } from '../../store/organization/action';
import { loadModels, setSelectedModelIdEntityAction } from "../../store/model/actions";
import { Subscription } from "rxjs";
import { selectApplications } from "../../store/application/selectors";
import { filter, take } from "rxjs/operators";
import { getUserInfo } from "../../store/userInfo/action";
import { OrganizationEnService } from "../../store/organization/organization-entitys.service";
import { selectModels } from "../../store/model/selectors";

@Injectable()
export class TestModelPageService {
  private appsSub: Subscription;
  private modelSub: Subscription;
  private appId: string;
  private orgId: string;
  private modelId: string;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private store: Store<AppState>,
    private organizationEnService: OrganizationEnService
  ) {
  }

  initUrlBindingStreams() {
    this.orgId = this.route.snapshot.queryParams.org;
    this.appId = this.route.snapshot.queryParams.app;
    this.modelId = this.route.snapshot.queryParams.model;

    if (this.appId && this.orgId && this.modelId) {
      this.store.dispatch(setSelectedAppIdEntityAction({ selectedAppId: this.appId }));
      this.store.dispatch(setSelectedId({ selectId: this.orgId }));
      this.store.dispatch(setSelectedModelIdEntityAction({ selectedModelId: this.modelId }));
      this.appsSub = this.store.select(selectApplications).pipe(
        filter(apps => !apps.length),
        take(1)
      ).subscribe(() => {
        this.fetchApps();
      });
      this.modelSub = this.store.select(selectModels).pipe(
        filter(models => !models.length),
        take(1)
      ).subscribe(() => {
        this.fetchModels();
      }
      )
    } else {
      this.router.navigate([ROUTERS_URL.HOME]);
    }
  }

  fetchApps() {
    this.store.dispatch(loadApplications({ organizationId: this.orgId }));
    this.store.dispatch(getUserInfo());
    this.organizationEnService.getAll();
  }

  fetchModels() {
    this.store.dispatch(loadModels({ organizationId: this.orgId, applicationId: this.appId }));
  }

  clearSelectedModelId() {
    this.store.dispatch(setSelectedModelIdEntityAction({ selectedModelId: null }));
  }

  unSubscribe() {
    if (this.appsSub) {
      this.appsSub.unsubscribe();
    }
    if (this.modelSub) {
      this.modelSub.unsubscribe();
    }
  }
}

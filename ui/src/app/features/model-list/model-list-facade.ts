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
import { combineLatest, Observable, Subscription } from 'rxjs';
import { IFacade } from 'src/app/data/facade/IFacade';
import { Model } from 'src/app/data/model';
import { AppState } from 'src/app/store';
import { selectCurrentAppId, selectUserRollForSelectedApp } from 'src/app/store/application/selectors';
import { createModel, deleteModel, loadModels, updateModel } from 'src/app/store/model/actions';
import { selectModels, selectPendingModel } from 'src/app/store/model/selectors';
import { selectCurrentOrganizationId } from 'src/app/store/organization/selectors';

@Injectable()
export class ModelListFacade implements IFacade {
  models$: Observable<Model[]>;
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  selectedOrganization$: Observable<string>;
  selectedApplication$: Observable<string>;

  private currentArgsAndApplicationSubscription: Subscription;
  selectedOrganizationId: string;
  selectedApplicationId: string;

  constructor(private store: Store<AppState>) {
    this.models$ = store.select(selectModels);
    this.isLoading$ = store.select(selectPendingModel);
    this.selectedOrganization$ = store.select(selectCurrentOrganizationId);
    this.selectedApplication$ = store.select(selectCurrentAppId);
    this.userRole$ = store.select(selectUserRollForSelectedApp);
  }

  initSubscriptions(): void {
    this.currentArgsAndApplicationSubscription = combineLatest(
      this.selectedOrganization$,
      this.selectedApplication$
    ).subscribe((ObservableResult) => {
      if (ObservableResult[0] !== null && ObservableResult[1] !== null) {
        this.selectedOrganizationId = ObservableResult[0];
        this.selectedApplicationId = ObservableResult[1];

        this.loadModels();
      }
    });
  }

  loadModels(): void {
    this.store.dispatch(loadModels({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId
    }));
  }

  createModel(name: string): void {
    this.store.dispatch(createModel({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      name
    }));
  }

  renameModel(modelId: string, name: string): void {
    this.store.dispatch(updateModel({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      modelId,
      name,
    }));
  }

  deleteModel(modelId: string): void {
    this.store.dispatch(deleteModel({
      organizationId: this.selectedOrganizationId,
      applicationId: this.selectedApplicationId,
      modelId,
    }));
  }

  unsubscribe(): void {
    this.currentArgsAndApplicationSubscription.unsubscribe();
  }
}

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
import { Injectable, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { Application } from 'src/app/data/interfaces/application';
import { IFacade } from 'src/app/data/interfaces/IFacade';
import { Model } from 'src/app/data/interfaces/model';
import { AppState } from 'src/app/store';
import { loadAppUserEntityAction } from 'src/app/store/app-user/action';
import { selectCurrentApp } from 'src/app/store/application/selectors';
import { createModel, cloneModel, deleteModel, loadModels, updateModel } from 'src/app/store/model/action';
import { selectModels, selectPendingModel, selectUserRole } from 'src/app/store/model/selectors';
import { loadRolesEntity } from 'src/app/store/role/action';
import { loadUsersEntity } from 'src/app/store/user/action';

@Injectable()
export class ModelListFacade implements IFacade {
  models$: Observable<Model[]>;
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  selectedApplication$: Observable<Application>;
  selectedApplicationId: string;
  private selectedApplicationSubscription: Subscription;

  constructor(private store: Store<AppState>) {
    this.models$ = this.store.select(selectModels);
    this.isLoading$ = this.store.select(selectPendingModel);
    this.selectedApplication$ = this.store.select(selectCurrentApp);
    this.userRole$ = this.store.select(selectUserRole);
  }

  initSubscriptions(): void {
    this.selectedApplicationSubscription = this.selectedApplication$.subscribe(result => {
      if (result) {
        this.selectedApplicationId = result.id;
        this.loadModels();
        this.loadData();
      }
    });
  }

  loadData(): void {
    this.store.dispatch(
      loadAppUserEntityAction({
        applicationId: this.selectedApplicationId,
      })
    );
    this.store.dispatch(loadRolesEntity());
    this.store.dispatch(loadUsersEntity());
  }

  loadModels(): void {
    this.store.dispatch(
      loadModels({
        applicationId: this.selectedApplicationId,
      })
    );
  }

  createModel(name: string, type: string): void {
    this.store.dispatch(
      createModel({
        model: {
          applicationId: this.selectedApplicationId,
          name,
          type,
        },
      })
    );
  }

  cloneModel(modelId: string, name: string): void {
    this.store.dispatch(
      cloneModel({
        applicationId: this.selectedApplicationId,
        modelId,
        name,
      })
    );
  }

  renameModel(modelId: string, name: string): void {
    this.store.dispatch(
      updateModel({
        applicationId: this.selectedApplicationId,
        modelId,
        name,
      })
    );
  }

  deleteModel(modelId: string): void {
    this.store.dispatch(
      deleteModel({
        applicationId: this.selectedApplicationId,
        modelId,
      })
    );
  }

  unsubscribe(): void {
    this.selectedApplicationSubscription.unsubscribe();
  }
}

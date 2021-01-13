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
import { map } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { IFacade } from 'src/app/data/interfaces/IFacade';
import { Model } from 'src/app/data/interfaces/model';
import { AppState } from 'src/app/store';
import { selectCurrentAppId, selectUserRollForSelectedApp } from 'src/app/store/application/selectors';
import { createModel, deleteModel, loadModels, updateModel } from 'src/app/store/model/actions';
import { selectModels, selectPendingModel } from 'src/app/store/model/selectors';
import { selectCurrentUserRole } from 'src/app/store/user/selectors';

@Injectable()
export class ModelListFacade implements IFacade {
  models$: Observable<Model[]>;
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  selectedApplication$: Observable<string>;
  selectedApplicationId: string;
  private selectedApplicationSubscription: Subscription;

  constructor(private store: Store<AppState>) {
    this.models$ = this.store.select(selectModels);
    this.isLoading$ = this.store.select(selectPendingModel);
    this.selectedApplication$ = this.store.select(selectCurrentAppId);
    this.userRole$ = combineLatest([this.store.select(selectUserRollForSelectedApp), this.store.select(selectCurrentUserRole)]).pipe(
      map(([applicationRole, globalRole]) =>
        // the global role (if OWNER or ADMINISTRATOR) should prevail on the application role
        globalRole !== Role.User ? globalRole : applicationRole
      )
    );
  }

  initSubscriptions(): void {
    this.selectedApplicationSubscription = this.selectedApplication$.subscribe(result => {
      if (result !== null) {
        this.selectedApplicationId = result;
        this.loadModels();
      }
    });
  }

  loadModels(): void {
    this.store.dispatch(
      loadModels({
        applicationId: this.selectedApplicationId,
      })
    );
  }

  createModel(name: string): void {
    this.store.dispatch(
      createModel({
        applicationId: this.selectedApplicationId,
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

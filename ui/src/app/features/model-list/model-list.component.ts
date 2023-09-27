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
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subscription } from 'rxjs';
import { filter, first, map, tap } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { Model } from 'src/app/data/interfaces/model';
import { ITableConfig } from 'src/app/features/table/table.component';

import { Routes } from '../../data/enums/routers-url.enum';
import { EditDialogComponent } from '../edit-dialog/edit-dialog.component';
import { ModelListFacade } from './model-list-facade';
import { ModelCreateDialogComponent } from '../mode-create-dialog/model-create-dialog.component';
import { ModelCloneDialogComponent } from '../model-clone-dialog/model-clone-dialog.component';
import { Application } from 'src/app/data/interfaces/application';

@Component({
  selector: 'app-model-list',
  templateUrl: './model-list.component.html',
  styleUrls: ['./model-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModelListComponent implements OnInit, OnDestroy {
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  tableConfig$: Observable<ITableConfig>;
  currentApp$: Observable<Application>;
  roleSubscription: Subscription;
  models: Model[];
  roleEnum = Role;
  columns = [
    { title: 'name', property: 'name' },
    { title: 'apiKey', property: 'apiKey' },
    { title: 'type', property: 'type' },
    { title: 'actions', property: 'id' },
  ];
  search = '';

  constructor(
    private modelListFacade: ModelListFacade,
    public dialog: MatDialog,
    private router: Router,
    private translate: TranslateService
  ) {
    this.modelListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.currentApp$ = this.modelListFacade.selectedApplication$;
    this.isLoading$ = this.modelListFacade.isLoading$;
    this.userRole$ = this.modelListFacade.userRole$;

    this.roleSubscription = this.userRole$.subscribe(role => {
      if (!role) {
        this.router.navigate([Routes.Home]);
      }
    });

    this.tableConfig$ = this.modelListFacade.models$.pipe(
      tap(models => (this.models = models)),
      map(models => ({
        columns: this.columns,
        data: models,
      }))
    );
  }

  edit(model: Model) {
    const dialog = this.dialog.open(EditDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        type: this.translate.instant('models.header'),
        label: this.translate.instant('models.type'),
        errorMsg: this.translate.instant('models.error_msg'),
        models: this.models,
        entityName: model.name,
        entityType: model.type,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(res => res),
        tap(res => {
          res.update ? this.modelListFacade.renameModel(model.id, res.name) : this.modelListFacade.deleteModel(model.id);
        })
      )
      .subscribe();
  }

  clone(model: Model) {
    const dialog = this.dialog.open(ModelCloneDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('models.header'),
        errorMsg: this.translate.instant('models.error_msg'),
        models: this.models,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(name => name)
      )
      .subscribe(name => this.modelListFacade.cloneModel(model.id, name));
  }

  test(model: Model) {
    this.router.navigate([Routes.TestModel], {
      queryParams: {
        app: this.modelListFacade.selectedApplicationId,
        model: model.id,
        type: model.type,
      },
    });
  }

  mangeCollection(model: Model): void {
    this.router.navigate([Routes.ManageCollection], {
      queryParams: {
        app: this.modelListFacade.selectedApplicationId,
        model: model.id,
        type: model.type,
      },
    });
  }

  dashboard(model: Model): void {
    this.router.navigate([Routes.Dashboard], {
      queryParams: {
        app: this.modelListFacade.selectedApplicationId,
        model: model.id,
        type: model.type,
      },
    });
  }

  onCreateNewModel(): void {
    const dialog = this.dialog.open(ModelCreateDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('models.header'),
        errorMsg: this.translate.instant('models.error_msg'),
        models: this.models,
      },
    });

    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(data => data && data.entityName && data.type)
      )
      .subscribe(data => this.modelListFacade.createModel(data.entityName, data.type));
  }

  onSearch(event: string) {
    this.search = event;
  }

  ngOnDestroy(): void {
    this.roleSubscription.unsubscribe();
    this.modelListFacade.unsubscribe();
  }
}

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
import { Observable } from 'rxjs';
import { first, map } from 'rxjs/operators';
import { Role } from 'src/app/data/enums/role.enum';
import { Model } from 'src/app/data/interfaces/model';
import { CreateDialogComponent } from 'src/app/features/create-dialog/create-dialog.component';
import { ITableConfig } from 'src/app/features/table/table.component';

import { Routes } from '../../data/enums/routers-url.enum';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { EditDialogComponent } from '../edit-dialog/edit-dialog.component';
import { ModelListFacade } from './model-list-facade';

@Component({
  selector: 'app-model-list',
  templateUrl: './model-list.component.html',
  styleUrls: ['./model-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModelListComponent implements OnInit, OnDestroy {
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  errorMessage: string;
  tableConfig$: Observable<ITableConfig>;
  roleEnum = Role;
  columns = [
    { title: 'name', property: 'name' },
    { title: 'apiKey', property: 'apiKey' },
    { title: 'copyKey', property: 'copyKey' },
    { title: 'actions', property: 'id' },
  ];

  constructor(
    private modelListFacade: ModelListFacade,
    public dialog: MatDialog,
    private router: Router,
    private translate: TranslateService
  ) {
    this.modelListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.modelListFacade.isLoading$;
    this.userRole$ = this.modelListFacade.userRole$;
    this.tableConfig$ = this.modelListFacade.models$.pipe(
      map(models => ({
        columns: this.columns,
        data: models,
      }))
    );
  }

  copyApiKey(apiKey: string) {
    const input = document.createElement('input');
    input.setAttribute('value', apiKey);
    document.body.appendChild(input);
    input.select();
    document.execCommand('copy');
    document.body.removeChild(input);
  }

  edit(model: Model) {
    const dialog = this.dialog.open(EditDialogComponent, {
      width: '400px',
      data: {
        entityType: this.translate.instant('models.header'),
        entityName: model.name,
      },
    });

    dialog
      .afterClosed()
      .pipe(first())
      .subscribe(name => {
        if (name) {
          this.modelListFacade.renameModel(model.id, name);
        }
      });
  }

  delete(model: Model) {
    const dialog = this.dialog.open(DeleteDialogComponent, {
      width: '400px',
      data: {
        entityType: this.translate.instant('models.header'),
        entityName: model.name,
      },
    });

    dialog
      .afterClosed()
      .pipe(first())
      .subscribe(result => {
        if (result) {
          this.modelListFacade.deleteModel(model.id);
        }
      });
  }

  test(model: Model) {
    this.router.navigate([Routes.TestModel], {
      queryParams: {
        app: this.modelListFacade.selectedApplicationId,
        model: model.id,
      },
    });
  }

  onCreateNewModel(): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
      width: '300px',
      data: {
        entityType: this.translate.instant('models.header'),
      },
    });

    dialog
      .afterClosed()
      .pipe(first())
      .subscribe(name => {
        if (name) {
          this.modelListFacade.createModel(name);
        }
      });
  }

  ngOnDestroy(): void {
    this.modelListFacade.unsubscribe();
  }
}

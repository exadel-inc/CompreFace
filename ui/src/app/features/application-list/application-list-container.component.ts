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
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CreateDialogComponent } from 'src/app/features/create-dialog/create-dialog.component';
import { ITableConfig } from 'src/app/features/table/table.component';

import { Routes } from '../../data/enums/routers-url.enum';
import { ApplicationListFacade } from './application-list-facade';

@Component({
  selector: 'app-application-list-container',
  template: `
    <app-application-list
      [isLoading]="isLoading$ | async"
      [userRole]="userRole$ | async"
      [tableConfig]="tableConfig$ | async"
      (selectApp)="onClick($event)"
      (createApp)="onCreateNewApp()"
    >
    </app-application-list>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationListContainerComponent implements OnInit {
  isLoading$: Observable<boolean>;
  userRole$: Observable<string>;
  tableConfig$: Observable<ITableConfig>;

  constructor(
    private applicationFacade: ApplicationListFacade,
    private dialog: MatDialog,
    private router: Router,
    private translate: TranslateService
  ) {
    this.applicationFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.applicationFacade.isLoading$;
    this.userRole$ = this.applicationFacade.userRole$;

    this.tableConfig$ = this.applicationFacade.applications$.pipe(
      map(apps => ({
        columns: [
          { title: 'Name', property: 'name' },
          { title: 'Owner', property: 'owner' },
        ],
        data: apps.map(app => ({ id: app.id, name: app.name, owner: `${app.owner.firstName} ${app.owner.lastName}` })),
      }))
    );
  }

  onClick(application): void {
    this.router.navigate([Routes.Application], {
      queryParams: {
        app: application.id,
      },
    });
  }

  onCreateNewApp(): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
      width: '300px',
      data: {
        entityType: this.translate.instant('applications.header.title'),
        name: '',
      },
    });

    const dialogSubscription = dialog.afterClosed().subscribe(name => {
      if (name) {
        this.applicationFacade.createApplication(name);
        dialogSubscription.unsubscribe();
      }
    });
  }
}

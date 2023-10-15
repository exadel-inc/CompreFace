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
import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subscription } from 'rxjs';
import { first, filter, tap, map } from 'rxjs/operators';
import { CircleLoadingProgressEnum } from 'src/app/data/enums/circle-loading-progress.enum';

import { Application } from '../../data/interfaces/application';
import { Model } from '../../data/interfaces/model';
import { BreadcrumbsFacade } from '../breadcrumbs/breadcrumbs.facade';
import { EditDialogComponent } from '../edit-dialog/edit-dialog.component';
import { ManageAppUsersDialog } from '../manage-app-users-dialog/manage-app-users.component';

@Component({
  selector: 'app-breadcrumbs-container',
  template: ` <app-breadcrumbs
    [model]="model$ | async"
    [app]="app$ | async"
    [itemsInProgress]="itemsInProgress$ | async"
    [hideControls]="hideControls"
    [modelSelected]="modelSelected"
    [currentUserRole]="currentUserRole$ | async"
    (usersList)="onUsersList($event)"
    (appSettings)="onAppSettings($event)"
  >
  </app-breadcrumbs>`,
  styleUrls: ['./breadcrumbs.container.component.scss'],
})
export class BreadcrumbsContainerComponent implements OnInit {
  app$: Observable<Application>;
  model$: Observable<Model>;
  modelSelected: boolean;
  itemsInProgress$: Observable<boolean>;
  currentUserRole$: Observable<string>;
  applications: Application[];
  subs: Subscription;

  @Input() hideControls: boolean;

  constructor(
    private breadcrumbsFacade: BreadcrumbsFacade,
    private translate: TranslateService,
    private dialog: MatDialog,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.app$ = this.breadcrumbsFacade.app$;
    this.subs = this.breadcrumbsFacade.applications$.subscribe(apps => (this.applications = apps));
    this.model$ = this.breadcrumbsFacade.model$;
    this.modelSelected = !!this.route.snapshot.queryParams.model;
    this.currentUserRole$ = this.breadcrumbsFacade.currentUserRole$;
    this.itemsInProgress$ = this.breadcrumbsFacade.collectionItems$.pipe(
      map(collection => !!collection.find(item => item.status === CircleLoadingProgressEnum.InProgress))
    );
  }

  onAppSettings(app: Application): void {
    const fullName = app.owner.firstName + ' ' + app.owner.lastName;

    const dialog = this.dialog.open(EditDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        type: this.translate.instant('applications.header.title'),
        label: this.translate.instant('applications.header.owner'),
        errorMsg: this.translate.instant('applications.error_msg'),
        entityName: app.name,
        ownerName: fullName,
        models: this.applications,
      },
    });
    dialog
      .afterClosed()
      .pipe(
        first(),
        filter(res => res),
        tap(res => {
          res.update ? this.breadcrumbsFacade.rename(res.name, app) : this.breadcrumbsFacade.delete(app);
        })
      )
      .subscribe();
  }

  onUsersList(app: Application): void {
    let currentUserId;
    let currentUserRole;

    const collection$ = this.breadcrumbsFacade.appUsers$;

    const userSubs = this.breadcrumbsFacade.currentUserId$.subscribe(userId => (currentUserId = userId));
    const userRoleSubs = this.breadcrumbsFacade.currentUserRole$.subscribe(userRole => (currentUserRole = userRole));

    const dialog = this.dialog.open(ManageAppUsersDialog, {
      data: {
        collection: collection$,
        currentApp: app,
        currentUserId: currentUserId,
        currentUserRole: currentUserRole,
      },
    });

    const dialogSubs = dialog.afterClosed().subscribe(() => {
      userSubs.unsubscribe();
      dialogSubs.unsubscribe();
      userRoleSubs.unsubscribe();
    });
  }
}
